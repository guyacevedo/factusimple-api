package com.factusimple.api.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePrepaymentRequestDto {

    private String referenceCode;

    @NotNull
    private LocalDate receivedDate;

    @NotNull
    @DecimalMin(value = "0.01", message = "El anticipo debe ser mayor a 0")
    private BigDecimal amount;

    private String note;
}
