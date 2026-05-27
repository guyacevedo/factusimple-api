package com.factusimple.api.infrastructure.factus.dto.bill;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FactusBillItemTaxDto(
    @JsonProperty("code")
    String code,

    @JsonProperty("rate")
    BigDecimal rate
) {}
