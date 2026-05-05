package com.factusimple.api.auth.dto;

import com.factusimple.api.establishments.dto.EstablishmentRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    @NotBlank(message = "El nombre es requerido")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    private String phone;

    @NotNull(message = "La información del establecimiento es requerida")
    @Valid
    private EstablishmentRequestDto establishment;
}
