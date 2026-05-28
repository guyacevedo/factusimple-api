package com.factusimple.api.auth.dto;

import com.factusimple.api.establishment.dto.EstablishmentRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record RegisterRequestDto(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    String email,

    @NotBlank(message = "La contraseña es requerida")
    @jakarta.validation.constraints.Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$", message = "La contraseña debe contener mayúsculas, minúsculas y números")
    String password,

    @NotBlank(message = "El nombre es requerido")
    String firstName,

    @NotBlank(message = "El apellido es requerido")
    String lastName,

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    String phone,

    @NotNull(message = "El planId es requerido")
    UUID planId,

    String inviteCode,

    @NotNull(message = "La información del establecimiento es requerida")
    @Valid
    EstablishmentRequestDto establishment
) {}
