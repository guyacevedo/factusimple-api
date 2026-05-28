package com.factusimple.api.plan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlanRequestDto(
    @NotBlank(message = "Plan name is required")
    String name,

    @NotNull(message = "Max products is required")
    @Min(value = 1, message = "Max products must be at least 1")
    Integer maxProducts,

    @NotNull(message = "Max customers is required")
    @Min(value = 1, message = "Max customers must be at least 1")
    Integer maxCustomers,

    @NotNull(message = "Max invoices is required")
    @Min(value = 1, message = "Max invoices must be at least 1")
    Integer maxInvoices,

    Boolean requiredInvitedCode,

    String code,

    String description,

    Boolean isActive
) {}
