package com.factusimple.api.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentResponseDto(
    UUID id,
    String paymentForm,
    String paymentMethodCode,
    String referenceCode,
    BigDecimal amount,
    LocalDate dueDate
) {}
