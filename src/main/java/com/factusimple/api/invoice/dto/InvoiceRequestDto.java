package com.factusimple.api.invoice.dto;

import com.factusimple.api.infrastructure.factus.codes.InvoiceOperationType;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import com.factusimple.api.shared.dto.PaymentRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvoiceRequestDto(
    @NotBlank
    String referenceCode,

    @ValidFactusCode(InvoiceOperationType.class)
    String operationType,

    Boolean sendEmail,

    String observation,

    BigDecimal cashRounding,

    @NotNull(message = "El numberingRangeId es requerido")
    Integer numberingRangeId,

    @NotNull
    UUID customerId,

    @NotEmpty
    @Valid
    List<InvoiceItemRequestDto> items,

    @NotEmpty
    @Valid
    List<PaymentRequestDto> payments,

    @Valid
    List<InvoicePrepaymentRequestDto> prepayments,

    @Valid
    List<AllowanceChargeRequestDto> allowanceCharges
) {}
