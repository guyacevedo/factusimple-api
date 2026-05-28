package com.factusimple.api.creditnote.dto;

import com.factusimple.api.shared.dto.ItemTaxResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreditNoteItemResponseDto(
        UUID id,
        UUID productId,
        String codeReference,
        String name,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal discountRate,
        String unitMeasureCode,
        String standardCode,
        String note,
        List<ItemTaxResponseDto> taxes
) {}
