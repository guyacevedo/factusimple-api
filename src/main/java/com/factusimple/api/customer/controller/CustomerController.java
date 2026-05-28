package com.factusimple.api.customer.controller;

import com.factusimple.api.customer.dto.CustomerRequestDto;
import com.factusimple.api.customer.dto.CustomerResponseDto;
import com.factusimple.api.customer.service.CustomerService;
import com.factusimple.api.infrastructure.filter.CustomUserDetails;
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
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CustomerRequestDto requestDto) {
        CustomerResponseDto dto = customerService.create(principal.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Cliente creado", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<CustomerResponseDto>>> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<CustomerResponseDto> customersPage = customerService.list(principal.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Clientes obtenidos", PageResponseDto.from(customersPage)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> get(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        CustomerResponseDto dto = customerService.get(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Cliente obtenido", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequestDto requestDto) {
        CustomerResponseDto dto = customerService.update(principal.getUserId(), id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success("Cliente actualizado", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        customerService.delete(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Cliente eliminado", null));
    }
}
