package com.factusimple.api.creditnote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreditNotePaymentResponseDto(
        UUID id,
        String paymentForm,
        String paymentMethodCode,
        String referenceCode,
        BigDecimal amount,
        LocalDate dueDate
) {}
