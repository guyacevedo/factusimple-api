package com.factusimple.api.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponseDto(
    UUID id,
    UUID establishmentId,
    String idTypeCode,
    String identification,
    String dv,
    String legalOrgCode,
    String company,
    String names,
    String tradeName,
    String address,
    String email,
    String phone,
    String municipalityCode,
    String tributeCode,
    String fiscalResponsibility,
    BigDecimal creditLimit,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
