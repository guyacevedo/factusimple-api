package com.factusimple.api.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentResponseDto {

    private UUID id;
    private String paymentForm;
    private String paymentMethodCode;
    private String referenceCode;
    private BigDecimal amount;
    private LocalDate dueDate;
}
