package com.factusimple.api.customer.dto;

import com.factusimple.api.infrastructure.factus.codes.FiscalResponsibilityCode;
import com.factusimple.api.infrastructure.factus.codes.IdentityDocumentType;
import com.factusimple.api.infrastructure.factus.codes.LegalOrgCode;
import com.factusimple.api.infrastructure.factus.codes.TributeCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDto {

    @NotBlank(message = "El tipo de identificación es requerido")
    @ValidFactusCode(IdentityDocumentType.class)
    private String idTypeCode;

    @NotBlank(message = "La identificación es requerida")
    private String identification;

    private String dv;

    @ValidFactusCode(LegalOrgCode.class)
    private String legalOrgCode;

    private String company;             // requerido si legalOrgCode = "1"

    private String names;               // requerido si legalOrgCode = "2"

    private String tradeName;

    private String address;

    @Email(message = "El email del cliente debe ser válido")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    private String phone;

    private String municipalityCode;

    @ValidFactusCode(TributeCode.class)
    private String tributeCode;

    @ValidFactusCode(FiscalResponsibilityCode.class)
    private String fiscalResponsibility;

    private BigDecimal creditLimit;

    private Boolean isActive;
}
