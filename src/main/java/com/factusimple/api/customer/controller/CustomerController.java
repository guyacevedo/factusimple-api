package com.factusimple.api.customer.controller;

import com.factusimple.api.customer.dto.CustomerRequestDto;
import com.factusimple.api.customer.dto.CustomerResponseDto;
import com.factusimple.api.customer.service.CustomerService;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
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
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CustomerRequestDto requestDto) {
        User user = resolveUser(principal);
        CustomerResponseDto dto = customerService.create(user.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Cliente creado", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<CustomerResponseDto>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = resolveUser(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerResponseDto> customersPage = customerService.list(user.getId(), pageable);

        PageResponseDto<CustomerResponseDto> pageResponse = PageResponseDto.<CustomerResponseDto>builder()
                .content(customersPage.getContent())
                .pageNumber(customersPage.getNumber())
                .pageSize(customersPage.getSize())
                .totalElements(customersPage.getTotalElements())
                .totalPages(customersPage.getTotalPages())
                .isLast(customersPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponseDto.success("Clientes obtenidos", pageResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> get(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        CustomerResponseDto dto = customerService.get(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Cliente obtenido", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequestDto requestDto) {
        User user = resolveUser(principal);
        CustomerResponseDto dto = customerService.update(user.getId(), id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success("Cliente actualizado", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        customerService.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Cliente eliminado", null));
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", principal.getUsername()));
    }
}
