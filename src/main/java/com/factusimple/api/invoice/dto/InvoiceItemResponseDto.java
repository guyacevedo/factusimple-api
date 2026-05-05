package com.factusimple.api.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemResponseDto {

    private UUID id;
    private UUID productId;
    private String codeReference;
    private String name;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountRate;
    private String unitMeasureCode;
    private String standardCode;
    private String note;
    private List<InvoiceItemTaxResponseDto> taxes;
}
