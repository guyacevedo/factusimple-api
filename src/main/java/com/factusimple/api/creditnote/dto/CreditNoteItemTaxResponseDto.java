package com.factusimple.api.creditnote.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditNoteItemTaxResponseDto(
        UUID id,
        String taxCode,
        BigDecimal taxRate,
        Boolean isWithholding
) {}
