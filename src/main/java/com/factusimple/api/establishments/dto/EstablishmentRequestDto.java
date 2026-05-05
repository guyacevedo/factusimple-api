package com.factusimple.api.establishments.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentRequestDto {

    @NotBlank(message = "La razón social es requerida")
    private String name;

    private String address;

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    private String phoneNumber;

    @Email(message = "El email del establecimiento debe ser válido")
    private String email;

    private String municipalityCode; // validar

    @NotBlank(message = "El NIT es requerido")
    private String nit;

    private String dv;

    private String legalOrgCode;        // "1" = Persona Jurídica, "2" = Persona Natural - Validar

    private String tributeCode;         // default "ZZ" - Validar

    private String fiscalResponsibility; // validar

    private String resolutionPrefix;

    private Long resolutionFrom;

    private Long resolutionTo;

    private Long resolutionCurrent;

    private LocalDate resolutionExpiry;

    private Long numberingRangeId; // validar
}
