package com.factusimple.api.auth.controller;

import com.factusimple.api.auth.dto.ActivateAccountRequestDto;
import com.factusimple.api.auth.dto.ForgotPasswordRequestDto;
import com.factusimple.api.auth.dto.LoginRequestDto;
import com.factusimple.api.auth.dto.LoginResponseDto;
import com.factusimple.api.auth.dto.RefreshTokenRequestDto;
import com.factusimple.api.auth.dto.RegisterRequestDto;
import com.factusimple.api.auth.dto.ResetPasswordRequestDto;
import com.factusimple.api.auth.service.AuthService;
import com.factusimple.api.shared.dto.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {
        LoginResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Usuario registrado exitosamente", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDto.success("Login exitoso", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        LoginResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponseDto.success("Token renovado", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            authService.logout(refreshToken);
        }
        return ResponseEntity.ok(ApiResponseDto.success("Logout exitoso", null));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponseDto<Void>> activateAccount(
            @Valid @RequestBody ActivateAccountRequestDto request) {
        authService.activateAccount(request);
        return ResponseEntity.ok(ApiResponseDto.success("Cuenta activada exitosamente", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {
        String resetToken = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponseDto.success("Token de reset generado", resetToken));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponseDto.success("Contraseña actualizada exitosamente", null));
    }
}