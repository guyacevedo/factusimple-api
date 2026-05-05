package com.factusimple.api.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowanceChargeResponseDto {

    private UUID id;
    private String conceptType;
    private Boolean isSurcharge;
    private String reason;
    private BigDecimal baseAmount;
    private BigDecimal amount;
}
