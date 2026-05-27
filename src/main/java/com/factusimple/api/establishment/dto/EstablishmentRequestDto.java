package com.factusimple.api.establishment.dto;

import com.factusimple.api.infrastructure.factus.codes.FiscalResponsibilityCode;
import com.factusimple.api.infrastructure.factus.codes.LegalOrgCode;
import com.factusimple.api.infrastructure.factus.codes.TributeCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EstablishmentRequestDto(
    @NotBlank(message = "La razón social es requerida")
    String name,

    String address,

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    String phoneNumber,

    @Email(message = "El email del establecimiento debe ser válido")
    String email,

    String municipalityCode,

    @NotBlank(message = "El NIT es requerido")
    String nit,

    String dv,

    @ValidFactusCode(LegalOrgCode.class)
    String legalOrgCode,

    @ValidFactusCode(TributeCode.class)
    String tributeCode,

    @ValidFactusCode(FiscalResponsibilityCode.class)
    String fiscalResponsibility
) {}
