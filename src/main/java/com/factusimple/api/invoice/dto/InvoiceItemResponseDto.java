package com.factusimple.api.invoice.dto;

import com.factusimple.api.shared.dto.ItemTaxResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvoiceItemResponseDto(
    UUID id,
    UUID productId,
    String codeReference,
    String name,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal discountRate,
    String unitMeasureCode,
    String standardCode,
    String note,
    List<ItemTaxResponseDto> taxes
) {}
