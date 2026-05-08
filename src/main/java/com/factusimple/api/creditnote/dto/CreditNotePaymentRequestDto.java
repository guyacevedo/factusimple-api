package com.factusimple.api.creditnote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditNotePaymentRequestDto(
        @NotBlank String paymentForm,
        @NotBlank String paymentMethodCode,
        String referenceCode,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        LocalDate dueDate
) {}
