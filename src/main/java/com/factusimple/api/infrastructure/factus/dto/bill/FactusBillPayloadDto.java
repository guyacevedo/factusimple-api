package com.factusimple.api.infrastructure.factus.dto.bill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactusBillPayloadDto(
    @JsonProperty("numbering_range_id")
    String numberingRangeId,

    @JsonProperty("reference_code")
    String referenceCode,

    @JsonProperty("document_type")
    String documentType,

    @JsonProperty("operation_type")
    String operationType,

    @JsonProperty("send_email")
    Boolean sendEmail,

    @JsonProperty("observation")
    String observation,

    @JsonProperty("cash_rounding")
    BigDecimal cashRounding,

    @JsonProperty("establishment")
    FactusBillEstablishmentDto establishment,

    @JsonProperty("customer")
    FactusBillCustomerDto customer,

    @JsonProperty("items")
    List<FactusBillItemDto> items,

    @JsonProperty("payment_details")
    List<FactusBillPaymentDto> paymentDetails,

    @JsonProperty("prepayments")
    List<FactusBillPrepaymentDto> prepayments,

    @JsonProperty("allowance_charges")
    List<FactusBillAllowanceChargeDto> allowanceCharges
) {}
