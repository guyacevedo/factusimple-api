package com.factusimple.api.product.controller;

import com.factusimple.api.infrastructure.filter.CustomUserDetails;
import com.factusimple.api.product.dto.ProductRequestDto;
import com.factusimple.api.product.dto.ProductResponseDto;
import com.factusimple.api.product.service.ProductService;
import com.factusimple.api.shared.dto.ApiResponseDto;
import com.factusimple.api.shared.dto.PageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto dto = productService.create(principal.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Producto creado", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductResponseDto>>> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> productsPage = productService.list(principal.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Productos obtenidos", PageResponseDto.from(productsPage)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> get(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        ProductResponseDto dto = productService.get(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto obtenido", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto dto = productService.update(principal.getUserId(), id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success("Producto actualizado", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        productService.delete(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto eliminado", null));
    }
}
