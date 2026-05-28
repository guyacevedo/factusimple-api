package com.factusimple.api.infrastructure.factus.dto.bill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusBillCustomerDto(
    @JsonProperty("identification_document_code")
    String identificationDocumentCode,

    @JsonProperty("identification")
    String identification,

    @JsonProperty("dv")
    String dv,

    @JsonProperty("legal_organization_id")
    String legalOrganizationId,

    @JsonProperty("tribute_id")
    String tributeId,

    @JsonProperty("names")
    String names,

    @JsonProperty("company")
    String company,

    @JsonProperty("trade_name")
    String tradeName,

    @JsonProperty("address")
    String address,

    @JsonProperty("email")
    String email,

    @JsonProperty("phone")
    String phone,

    @JsonProperty("municipality_id")
    String municipalityId
) {}
