package com.factusimple.api.factuscodes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumberingRangeDto {
    private Integer id;
    private String document;
    private String prefix;
    private Long from;
    private Long to;
    private Long current;

    @JsonProperty("resolution_number")
    private String resolutionNumber;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("technical_key")
    private String technicalKey;

    @JsonProperty("is_expired")
    private Boolean isExpired;

    @JsonProperty("is_active")
    private Boolean isActive;
}
