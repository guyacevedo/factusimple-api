package com.factusimple.api.plan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequestDto {

    @NotBlank(message = "Plan name is required")
    private String name;

    @NotNull(message = "Max products is required")
    @Min(value = 1, message = "Max products must be at least 1")
    private Integer maxProducts;

    @NotNull(message = "Max customers is required")
    @Min(value = 1, message = "Max customers must be at least 1")
    private Integer maxCustomers;

    @NotNull(message = "Max invoices is required")
    @Min(value = 1, message = "Max invoices must be at least 1")
    private Integer maxInvoices;

    private Boolean requiredInvitedCode;

    private String description;

    private Boolean isActive;
}
