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
public class InvoicePrepaymentResponseDto {

    private UUID id;
    private String referenceCode;
    private LocalDate receivedDate;
    private BigDecimal amount;
    private String note;
}
