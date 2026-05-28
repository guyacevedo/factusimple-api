package com.factusimple.api.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoicePrepaymentRequestDto(
    String referenceCode,

    @NotNull
    LocalDate receivedDate,

    @NotNull
    @DecimalMin(value = "0.01", message = "El anticipo debe ser mayor a 0")
    BigDecimal amount,

    String note
) {}
