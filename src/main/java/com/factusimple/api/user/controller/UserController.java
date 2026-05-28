package com.factusimple.api.user.controller;

import com.factusimple.api.infrastructure.filter.CustomUserDetails;
import com.factusimple.api.plan.dto.PlanResponseDto;
import com.factusimple.api.shared.dto.ApiResponseDto;
import com.factusimple.api.shared.dto.PageResponseDto;
import com.factusimple.api.user.dto.ChangePlanRequestDto;
import com.factusimple.api.user.dto.ChangePasswordRequestDto;
import com.factusimple.api.user.dto.UserResponseDto;
import com.factusimple.api.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<UserResponseDto>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<UserResponseDto> usersPage = userService.listUsers(pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Users retrieved successfully", PageResponseDto.from(usersPage)));
    }

    @PatchMapping("/me/password")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponseDto.success("Contraseña actualizada exitosamente", null));
    }

    @PatchMapping("/me/plan")
    @PreAuthorize("hasRole('ESTABLISHMENT')")
    public ResponseEntity<ApiResponseDto<Void>> changePlan(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChangePlanRequestDto request) {
        userService.changePlan(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponseDto.success("Plan actualizado exitosamente", null));
    }
}
