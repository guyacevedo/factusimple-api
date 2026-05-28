package com.factusimple.api.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoicePrepaymentResponseDto(
    UUID id,
    String referenceCode,
    LocalDate receivedDate,
    BigDecimal amount,
    String note
) {}
