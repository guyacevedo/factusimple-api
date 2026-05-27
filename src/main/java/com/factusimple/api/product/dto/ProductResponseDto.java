package com.factusimple.api.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDto(
    UUID id,
    UUID establishmentId,
    String sku,
    String name,
    BigDecimal price,
    BigDecimal stock,
    String unitMeasureCode,
    String standardCode,
    String taxCode,
    BigDecimal taxRate,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
