package com.factusimple.api.invoice.dto;

import com.factusimple.api.customer.dto.CustomerResponseDto;
import com.factusimple.api.invoice.entity.InvoiceStatus;
import com.factusimple.api.shared.dto.AllowanceChargeResponseDto;
import com.factusimple.api.shared.dto.PaymentResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponseDto(
    UUID id,
    UUID establishmentId,
    CustomerResponseDto customer,
    String referenceCode,
    String documentType,
    String operationType,
    Boolean sendEmail,
    String observation,
    BigDecimal cashRounding,
    InvoiceStatus status,
    BigDecimal subtotal,
    BigDecimal totalTaxes,
    BigDecimal totalDiscounts,
    BigDecimal total,
    String cufe,
    String xmlUrl,
    String factusNumber,
    String factusError,
    List<InvoiceItemResponseDto> items,
    List<PaymentResponseDto> payments,
    List<InvoicePrepaymentResponseDto> prepayments,
    List<AllowanceChargeResponseDto> allowanceCharges,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
