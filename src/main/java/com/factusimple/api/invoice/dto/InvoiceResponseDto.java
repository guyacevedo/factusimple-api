package com.factusimple.api.invoice.dto;

import com.factusimple.api.invoice.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDto {

    private UUID id;
    private UUID establishmentId;
    private UUID customerId;
    private String referenceCode;
    private String documentType;
    private String operationType;
    private Boolean sendEmail;
    private String observation;
    private BigDecimal cashRounding;
    private InvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal totalTaxes;
    private BigDecimal totalDiscounts;
    private BigDecimal total;
    private String cufe;
    private String xmlUrl;
    private String factusNumber;
    private String factusError;
    private List<InvoiceItemResponseDto> items;
    private List<InvoicePaymentResponseDto> payments;
    private List<InvoicePrepaymentResponseDto> prepayments;
    private List<AllowanceChargeResponseDto> allowanceCharges;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
