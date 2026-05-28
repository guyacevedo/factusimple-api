package com.factusimple.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    String email
) {}
