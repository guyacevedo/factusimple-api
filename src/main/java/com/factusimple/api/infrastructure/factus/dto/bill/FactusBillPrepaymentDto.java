package com.factusimple.api.infrastructure.factus.dto.bill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusBillPrepaymentDto(
    @JsonProperty("reference_code")
    String referenceCode,

    @JsonProperty("received_date")
    String receivedDate,

    @JsonProperty("amount")
    BigDecimal amount,

    @JsonProperty("note")
    String note
) {}
