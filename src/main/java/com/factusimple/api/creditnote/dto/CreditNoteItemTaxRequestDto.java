package com.factusimple.api.creditnote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreditNoteItemTaxRequestDto(
        @NotBlank String taxCode,
        @NotNull @DecimalMin("0.0") BigDecimal taxRate,
        @NotNull Boolean isWithholding
) {}
