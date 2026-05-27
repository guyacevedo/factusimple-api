package com.factusimple.api.auth.dto;

import com.factusimple.api.establishment.dto.EstablishmentRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterRequestDto(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    String email,

    @NotBlank(message = "La contraseña es requerida")
    String password,

    @NotBlank(message = "El nombre es requerido")
    String firstName,

    @NotBlank(message = "El apellido es requerido")
    String lastName,

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    String phone,

    @NotNull(message = "La información del establecimiento es requerida")
    @Valid
    EstablishmentRequestDto establishment
) {}
