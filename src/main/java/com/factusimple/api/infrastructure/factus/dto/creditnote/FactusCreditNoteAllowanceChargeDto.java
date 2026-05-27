package com.factusimple.api.infrastructure.factus.dto.creditnote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusCreditNoteAllowanceChargeDto(
    @JsonProperty("concept_type")
    String conceptType,

    @JsonProperty("is_surcharge")
    Boolean isSurcharge,

    @JsonProperty("reason")
    String reason,

    @JsonProperty("base_amount")
    BigDecimal baseAmount,

    @JsonProperty("amount")
    BigDecimal amount
) {}
