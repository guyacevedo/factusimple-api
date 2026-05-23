package com.factusimple.api.establishments.controller;

import com.factusimple.api.establishments.dto.EstablishmentRequestDto;
import com.factusimple.api.establishments.dto.EstablishmentResponseDto;
import com.factusimple.api.establishments.service.EstablishmentService;
import com.factusimple.api.infrastructure.filter.CustomUserDetails;
import com.factusimple.api.shared.dto.ApiResponseDto;
import com.factusimple.api.shared.dto.PageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private final EstablishmentService establishmentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<EstablishmentResponseDto>>> listEstablishments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<EstablishmentResponseDto> establishmentPage = establishmentService.listEstablishments(pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Establishments retrieved successfully", PageResponseDto.from(establishmentPage)));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<EstablishmentResponseDto>> getMine(
            @AuthenticationPrincipal CustomUserDetails principal) {
        EstablishmentResponseDto dto = establishmentService.getMine(principal.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("Establecimiento obtenido", dto));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<EstablishmentResponseDto>> updateMine(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody EstablishmentRequestDto requestDto) {
        EstablishmentResponseDto dto = establishmentService.update(principal.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponseDto.success("Establecimiento actualizado", dto));
    }
}
