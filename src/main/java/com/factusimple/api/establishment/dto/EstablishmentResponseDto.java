package com.factusimple.api.establishment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EstablishmentResponseDto(
    UUID id,
    UUID userId,
    String name,
    String address,
    String phoneNumber,
    String email,
    String municipalityCode,
    String nit,
    String dv,
    String legalOrgCode,
    String tributeCode,
    String fiscalResponsibility,
    Integer numberingRangeId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
