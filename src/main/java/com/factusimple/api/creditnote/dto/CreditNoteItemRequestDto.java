package com.factusimple.api.creditnote.dto;

import com.factusimple.api.shared.dto.ItemTaxRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreditNoteItemRequestDto(
        UUID productId,
        String codeReference,
        @NotBlank String name,
        @NotNull @DecimalMin("0.0001") BigDecimal quantity,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @DecimalMin("0.0") BigDecimal discountRate,
        String unitMeasureCode,
        String standardCode,
        String note,
        @NotEmpty @Valid List<ItemTaxRequestDto> taxes
) {}
