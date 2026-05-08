package com.factusimple.api.creditnote.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditNoteAllowanceChargeResponseDto(
        UUID id,
        String conceptType,
        Boolean isSurcharge,
        String reason,
        BigDecimal baseAmount,
        BigDecimal amount
) {}
