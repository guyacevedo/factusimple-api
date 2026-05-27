package com.factusimple.api.invoice.dto;

import com.factusimple.api.infrastructure.factus.codes.AllowanceChargeConceptCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AllowanceChargeRequestDto(
    @NotBlank
    @ValidFactusCode(AllowanceChargeConceptCode.class)
    String conceptType,

    @NotNull
    Boolean isSurcharge,

    @NotBlank
    String reason,

    @NotNull
    @DecimalMin(value = "0.0")
    BigDecimal baseAmount,

    @NotNull
    @DecimalMin(value = "0.0")
    BigDecimal amount
) {}
