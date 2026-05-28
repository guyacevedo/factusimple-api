package com.factusimple.api.infrastructure.factus.dto.creditnote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusCreditNotePaymentDto(
    @JsonProperty("payment_form")
    String paymentForm,

    @JsonProperty("payment_method_code")
    String paymentMethodCode,

    @JsonProperty("reference_code")
    String referenceCode,

    @JsonProperty("amount")
    BigDecimal amount,

    @JsonProperty("due_date")
    String dueDate
) {}
