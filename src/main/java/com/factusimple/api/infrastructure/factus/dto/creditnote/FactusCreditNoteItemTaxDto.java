package com.factusimple.api.infrastructure.factus.dto.creditnote;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FactusCreditNoteItemTaxDto(
    @JsonProperty("code")
    String code,

    @JsonProperty("rate")
    BigDecimal rate
) {}
