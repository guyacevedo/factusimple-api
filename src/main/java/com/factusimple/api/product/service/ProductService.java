package com.factusimple.api.product.service;

import com.factusimple.api.establishment.entity.Establishment;
import com.factusimple.api.establishment.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.*;
import com.factusimple.api.product.dto.ProductRequestDto;
import com.factusimple.api.product.dto.ProductResponseDto;
import com.factusimple.api.product.entity.Product;
import com.factusimple.api.product.mapper.ProductMapper;
import com.factusimple.api.product.repository.ProductRepository;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final EstablishmentService establishmentService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public ProductResponseDto create(UUID userId, ProductRequestDto requestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Establishment establishment = establishmentService.getEntityByUserId(userId);

        if (productRepository.existsBySkuAndEstablishmentId(requestDto.getSku(), establishment.getId())) {
            throw new ConflictException(
                    "Ya existe un producto con SKU '" + requestDto.getSku() + "' en este establecimiento");
        }

        Product product = productMapper.toEntity(requestDto);
        product.setEstablishment(establishment);
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }

        Product saved = productRepository.save(product);
        entityManager.flush();

        // Atomic increment: if limit reached, returns 0 (no update)
        int updated = userRepository.incrementProductsCountIfBelowLimit(userId, user.getPlan().getMaxProducts());
        if (updated == 0) {
            throw new ForbiddenException(
                    "Límite del plan alcanzado: " + user.getPlan().getMaxProducts() + " productos.");
        }

        log.info("Producto creado: id={}, sku={}, establishmentId={}",
                saved.getId(), saved.getSku(), establishment.getId());
        return productMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> list(UUID userId, Pageable pageable) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        return productRepository.findByEstablishmentId(establishment.getId(), pageable)
                .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto get(UUID userId, UUID productId) {
        return productMapper.toDto(requireOwned(userId, productId));
    }

    @Transactional
    public ProductResponseDto update(UUID userId, UUID productId, ProductRequestDto requestDto) {
        Product product = requireOwned(userId, productId);

        if (!product.getSku().equals(requestDto.getSku())
                && productRepository.existsBySkuAndEstablishmentId(
                        requestDto.getSku(), product.getEstablishment().getId())) {
            throw new ConflictException(
                    "Ya existe un producto con SKU '" + requestDto.getSku() + "' en este establecimiento");
        }

        productMapper.updateEntity(requestDto, product);
        Product saved = productRepository.save(product);
        log.info("Producto actualizado: id={}", saved.getId());
        return productMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID userId, UUID productId) {
        Product product = requireOwned(userId, productId);
        productRepository.delete(product);
        userRepository.decrementProductsCount(userId);
        log.info("Producto eliminado: id={}", productId);
    }

    private Product requireOwned(UUID userId, UUID productId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        return productRepository.findByIdAndEstablishmentId(productId, establishment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }
}
