package com.factusimple.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
    @NotBlank(message = "El refresh token es requerido")
    String refreshToken
) {}
