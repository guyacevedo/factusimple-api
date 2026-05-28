package com.factusimple.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    String email,

    @NotBlank(message = "El token es requerido")
    String resetToken,

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
             message = "La contraseña debe contener mayúsculas, minúsculas y números")
    String newPassword
) {}
