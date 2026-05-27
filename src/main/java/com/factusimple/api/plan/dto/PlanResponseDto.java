package com.factusimple.api.plan.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlanResponseDto(
    UUID id,
    String name,
    Integer maxProducts,
    Integer maxCustomers,
    Integer maxInvoices,
    Boolean isActive,
    String description,
    Boolean requiredInvitedCode,
    String code,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
