package com.factusimple.api.shared.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AllowanceChargeResponseDto(
    UUID id,
    String conceptType,
    Boolean isSurcharge,
    String reason,
    BigDecimal baseAmount,
    BigDecimal amount
) {}
