package com.factusimple.api.shared.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

public record ItemTaxRequestDto(
    @NotBlank
    String taxCode,

    @NotNull
    @DecimalMin(value = "0.0", message = "La tasa no puede ser negativa")
    BigDecimal taxRate,

    @Nullable
    Boolean isWithholding
) {}
