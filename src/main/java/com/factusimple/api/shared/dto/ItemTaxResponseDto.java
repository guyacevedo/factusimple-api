package com.factusimple.api.shared.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemTaxResponseDto(
    UUID id,
    String taxCode,
    BigDecimal taxRate,
    Boolean isWithholding
) {}
