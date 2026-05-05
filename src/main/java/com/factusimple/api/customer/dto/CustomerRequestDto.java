package com.factusimple.api.customer.dto;

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
    private String idTypeCode; // validar

    @NotBlank(message = "La identificación es requerida")
    private String identification;

    private String dv;

    private String legalOrgCode;        // "1" = PJ, "2" = PN - validar

    private String company;             // requerido si legalOrgCode = "1"

    private String names;               // requerido si legalOrgCode = "2"

    private String tradeName;

    private String address;

    @Email(message = "El email del cliente debe ser válido")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    private String phone;

    private String municipalityCode;

    private String tributeCode; // validar

    private String fiscalResponsibility; // validar

    private BigDecimal creditLimit;

    private Boolean isActive;
}
