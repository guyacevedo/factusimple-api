package com.factusimple.api.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemTaxResponseDto {

    private UUID id;
    private String taxCode;
    private BigDecimal taxRate;
    private Boolean isWithholding;
}
