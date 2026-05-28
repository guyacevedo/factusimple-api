package com.factusimple.api.infrastructure.factus.dto.creditnote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusCreditNoteCustomerDto(
    @JsonProperty("identification")
    String identification,

    @JsonProperty("legal_org_code")
    String legalOrgCode,

    @JsonProperty("names")
    String names,

    @JsonProperty("email")
    String email,

    @JsonProperty("phone")
    String phone,

    @JsonProperty("address")
    String address,

    @JsonProperty("municipality_code")
    String municipalityCode
) {}
