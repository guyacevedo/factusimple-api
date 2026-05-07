package com.factusimple.api.product.controller;

import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.product.dto.ProductRequestDto;
import com.factusimple.api.product.dto.ProductResponseDto;
import com.factusimple.api.product.service.ProductService;
import com.factusimple.api.shared.dto.ApiResponseDto;
import com.factusimple.api.shared.dto.PageResponseDto;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ProductRequestDto requestDto) {
        User user = resolveUser(principal);
        ProductResponseDto dto = productService.create(user.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Producto creado", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductResponseDto>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = resolveUser(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> productsPage = productService.list(user.getId(), pageable);

        PageResponseDto<ProductResponseDto> pageResponse = PageResponseDto.<ProductResponseDto>builder()
                .content(productsPage.getContent())
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .isLast(productsPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponseDto.success("Productos obtenidos", pageResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> get(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        ProductResponseDto dto = productService.get(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto obtenido", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequestDto requestDto) {
        User user = resolveUser(principal);
        ProductResponseDto dto = productService.update(user.getId(), id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success("Producto actualizado", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        productService.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto eliminado", null));
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", principal.getUsername()));
    }
}
