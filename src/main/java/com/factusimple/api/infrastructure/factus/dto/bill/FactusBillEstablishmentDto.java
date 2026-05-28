package com.factusimple.api.infrastructure.factus.dto.bill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusBillEstablishmentDto(
    @JsonProperty("name")
    String name,

    @JsonProperty("address")
    String address,

    @JsonProperty("phone_number")
    String phoneNumber,

    @JsonProperty("email")
    String email,

    @JsonProperty("municipality_code")
    String municipalityCode
) {}
