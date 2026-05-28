package com.factusimple.api.shared.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequestDto(
    @NotBlank
    String paymentForm,

    @NotBlank
    String paymentMethodCode,

    String referenceCode,

    @NotNull
    BigDecimal amount,

    LocalDate dueDate
) {}
