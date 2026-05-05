package com.factusimple.api.product.service;

import com.factusimple.api.establishments.entity.Establishment;
import com.factusimple.api.establishments.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.product.dto.ProductRequestDto;
import com.factusimple.api.product.dto.ProductResponseDto;
import com.factusimple.api.product.entity.Product;
import com.factusimple.api.product.mapper.ProductMapper;
import com.factusimple.api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final EstablishmentService establishmentService;

    @Transactional
    public ProductResponseDto create(UUID userId, ProductRequestDto requestDto) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);

        if (productRepository.existsBySkuAndEstablishmentId(requestDto.getSku(), establishment.getId())) {
            throw new ApiException(409,
                    "Ya existe un producto con SKU '" + requestDto.getSku() + "' en este establecimiento");
        }

        Product product = productMapper.toEntity(requestDto);
        product.setEstablishment(establishment);
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }

        Product saved = productRepository.save(product);
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
            throw new ApiException(409,
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
        log.info("Producto eliminado: id={}", productId);
    }

    private Product requireOwned(UUID userId, UUID productId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        return productRepository.findByIdAndEstablishmentId(productId, establishment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }
}
