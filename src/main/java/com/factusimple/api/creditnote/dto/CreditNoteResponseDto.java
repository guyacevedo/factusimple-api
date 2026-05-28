package com.factusimple.api.creditnote.dto;

import com.factusimple.api.creditnote.entity.CreditNoteStatus;
import com.factusimple.api.shared.dto.AllowanceChargeResponseDto;
import com.factusimple.api.shared.dto.PaymentResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreditNoteResponseDto(
        UUID id,
        UUID establishmentId,
        UUID invoiceId,
        String invoiceReferenceCode,
        String invoiceFactusNumber,
        String referenceCode,
        String correctionConceptCode,
        String customizationId,
        String observation,
        CreditNoteStatus status,
        String factusNumber,
        String cufe,
        String xmlUrl,
        String factusError,
        List<CreditNoteItemResponseDto> items,
        List<PaymentResponseDto> payments,
        List<AllowanceChargeResponseDto> allowanceCharges,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
