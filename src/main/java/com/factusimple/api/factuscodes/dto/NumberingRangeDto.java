package com.factusimple.api.factuscodes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NumberingRangeDto(
    Integer id,
    String document,
    String prefix,
    Long from,
    Long to,
    Long current,

    @JsonProperty("resolution_number")
    String resolutionNumber,

    @JsonProperty("start_date")
    String startDate,

    @JsonProperty("end_date")
    String endDate,

    @JsonProperty("technical_key")
    String technicalKey,

    @JsonProperty("is_expired")
    Boolean isExpired,

    @JsonProperty("is_active")
    Boolean isActive
) {}
