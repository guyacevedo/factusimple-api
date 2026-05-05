package com.factusimple.api.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemTaxRequestDto {

    /**
     * Código del impuesto/retención. Validar en el service contra TaxCode
     * (cuando isWithholding=false) o WithholdingTaxCode (cuando isWithholding=true).
     */
    @NotBlank
    private String taxCode;

    @NotNull
    @DecimalMin(value = "0.0", message = "La tasa no puede ser negativa")
    private BigDecimal taxRate;

    private Boolean isWithholding;      // default false
}
