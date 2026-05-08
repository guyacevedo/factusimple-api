package com.factusimple.api.creditnote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreditNoteAllowanceChargeRequestDto(
        @NotBlank String conceptType,
        @NotNull Boolean isSurcharge,
        String reason,
        @DecimalMin("0.0") BigDecimal baseAmount,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {}
