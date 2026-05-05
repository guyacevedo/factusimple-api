package com.factusimple.api.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private UUID id;
    private UUID establishmentId;
    private String sku;
    private String name;
    private BigDecimal price;
    private BigDecimal stock;
    private String unitMeasureCode;
    private String standardCode;
    private String taxCode;
    private BigDecimal taxRate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
