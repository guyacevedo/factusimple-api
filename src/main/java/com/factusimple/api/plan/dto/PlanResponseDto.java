package com.factusimple.api.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponseDto {

    private UUID id;
    private String name;
    private Integer maxProducts;
    private Integer maxCustomers;
    private Integer maxInvoices;
    private Boolean isActive;
    private String description;
    private Boolean requiredInvitedCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
