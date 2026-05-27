package com.factusimple.api.infrastructure.factus.dto.bill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusBillItemDto(
    @JsonProperty("code_reference")
    String codeReference,

    @JsonProperty("name")
    String name,

    @JsonProperty("quantity")
    BigDecimal quantity,

    @JsonProperty("price")
    BigDecimal price,

    @JsonProperty("discount_rate")
    BigDecimal discountRate,

    @JsonProperty("unit_measure_code")
    String unitMeasureCode,

    @JsonProperty("standard_code")
    String standardCode,

    @JsonProperty("note")
    String note,

    @JsonProperty("sku")
    String sku,

    @JsonProperty("taxes")
    List<FactusBillItemTaxDto> taxes,

    @JsonProperty("withholding_taxes")
    List<FactusBillItemTaxDto> withholdingTaxes
) {}
