package com.factusimple.api.establishments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentResponseDto {

    private UUID id;
    private UUID userId;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String municipalityCode;
    private String nit;
    private String dv;
    private String legalOrgCode;
    private String tributeCode;
    private String fiscalResponsibility;
    private String resolutionPrefix;
    private Long resolutionFrom;
    private Long resolutionTo;
    private Long resolutionCurrent;
    private LocalDate resolutionExpiry;
    private Long numberingRangeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
