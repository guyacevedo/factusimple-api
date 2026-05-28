package com.factusimple.api.customer.dto;

import com.factusimple.api.infrastructure.factus.codes.FiscalResponsibilityCode;
import com.factusimple.api.infrastructure.factus.codes.IdentityDocumentType;
import com.factusimple.api.infrastructure.factus.codes.LegalOrgCode;
import com.factusimple.api.infrastructure.factus.codes.TributeCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CustomerRequestDto(
    @NotBlank(message = "El tipo de identificación es requerido")
    @ValidFactusCode(IdentityDocumentType.class)
    String idTypeCode,

    @NotBlank(message = "La identificación es requerida")
    String identification,

    String dv,

    @ValidFactusCode(LegalOrgCode.class)
    String legalOrgCode,

    String company,

    String names,

    String tradeName,

    String address,

    @Email(message = "El email del cliente debe ser válido")
    String email,

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    String phone,

    String municipalityCode,

    @ValidFactusCode(TributeCode.class)
    String tributeCode,

    @ValidFactusCode(FiscalResponsibilityCode.class)
    String fiscalResponsibility,

    BigDecimal creditLimit,

    Boolean isActive
) {}
