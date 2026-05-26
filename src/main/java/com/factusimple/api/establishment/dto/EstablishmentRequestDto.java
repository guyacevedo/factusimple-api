package com.factusimple.api.establishment.dto;

import com.factusimple.api.infrastructure.factus.codes.FiscalResponsibilityCode;
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

    private String municipalityCode;

    @NotBlank(message = "El NIT es requerido")
    private String nit;

    private String dv;

    @ValidFactusCode(LegalOrgCode.class)
    private String legalOrgCode;

    @ValidFactusCode(TributeCode.class)
    private String tributeCode;

    @ValidFactusCode(FiscalResponsibilityCode.class)
    private String fiscalResponsibility;
}
