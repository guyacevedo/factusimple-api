package com.factusimple.api.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequestDto {

    @NotBlank
    private String referenceCode;

    private String documentType;        // default "01" - validar

    private String operationType;       // default "10" - validar

    private Boolean sendEmail;

    private String observation;

    private BigDecimal cashRounding;

    @NotNull
    private UUID customerId;

    @NotEmpty
    @Valid
    private List<InvoiceItemRequestDto> items;

    @NotEmpty
    @Valid
    private List<InvoicePaymentRequestDto> payments;

    @Valid
    private List<InvoicePrepaymentRequestDto> prepayments;

    @Valid
    private List<AllowanceChargeRequestDto> allowanceCharges;
}
