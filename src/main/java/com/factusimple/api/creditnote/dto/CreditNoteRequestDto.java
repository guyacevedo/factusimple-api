package com.factusimple.api.creditnote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreditNoteRequestDto(
        @NotBlank String referenceCode,
        @NotBlank String correctionConceptCode,
        String customizationId,
        UUID invoiceId,
        String observation,
        @NotEmpty @Valid List<CreditNoteItemRequestDto> items,
        @NotEmpty @Valid List<CreditNotePaymentRequestDto> payments,
        @Valid List<CreditNoteAllowanceChargeRequestDto> allowanceCharges
) {}
