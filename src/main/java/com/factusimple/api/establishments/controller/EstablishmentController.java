package com.factusimple.api.establishments.controller;

import com.factusimple.api.establishments.dto.EstablishmentRequestDto;
import com.factusimple.api.establishments.dto.EstablishmentResponseDto;
import com.factusimple.api.establishments.service.EstablishmentService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private final EstablishmentService establishmentService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<EstablishmentResponseDto>>> listEstablishments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<EstablishmentResponseDto> establishmentPage = establishmentService.listEstablishments(pageable);

        PageResponseDto<EstablishmentResponseDto> pageResponse = PageResponseDto.<EstablishmentResponseDto>builder()
                .content(establishmentPage.getContent())
                .pageNumber(establishmentPage.getNumber())
                .pageSize(establishmentPage.getSize())
                .totalElements(establishmentPage.getTotalElements())
                .totalPages(establishmentPage.getTotalPages())
                .isLast(establishmentPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponseDto.success("Establishments retrieved successfully", pageResponse));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<EstablishmentResponseDto>> getMine(
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        EstablishmentResponseDto dto = establishmentService.getMine(user.getId());
        return ResponseEntity.ok(ApiResponseDto.success("Establecimiento obtenido", dto));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<EstablishmentResponseDto>> updateMine(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody EstablishmentRequestDto requestDto) {
        User user = resolveUser(principal);
        EstablishmentResponseDto dto = establishmentService.update(user.getId(), requestDto);
        return ResponseEntity.ok(ApiResponseDto.success("Establecimiento actualizado", dto));
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", principal.getUsername()));
    }
}
