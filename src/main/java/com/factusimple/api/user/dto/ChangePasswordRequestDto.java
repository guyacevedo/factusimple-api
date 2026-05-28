package com.factusimple.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
    @NotBlank(message = "La contraseña actual es requerida")
    String currentPassword,

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
             message = "La contraseña debe contener mayúsculas, minúsculas y números")
    String newPassword
) {}
