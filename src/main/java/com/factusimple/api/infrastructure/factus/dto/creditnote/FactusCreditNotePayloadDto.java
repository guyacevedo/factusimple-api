package com.factusimple.api.infrastructure.factus.dto.creditnote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusCreditNotePayloadDto(
    @JsonProperty("reference_code")
    String referenceCode,

    @JsonProperty("correction_concept_code")
    String correctionConceptCode,

    @JsonProperty("customization_id")
    String customizationId,

    @JsonProperty("observation")
    String observation,

    @JsonProperty("bill_number")
    String billNumber,

    @JsonProperty("customer")
    FactusCreditNoteCustomerDto customer,

    @JsonProperty("items")
    List<FactusCreditNoteItemDto> items,

    @JsonProperty("payment_details")
    List<FactusCreditNotePaymentDto> paymentDetails,

    @JsonProperty("allowance_charges")
    List<FactusCreditNoteAllowanceChargeDto> allowanceCharges
) {}
