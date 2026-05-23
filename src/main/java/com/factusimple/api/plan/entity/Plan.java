package com.factusimple.api.plan.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "plans", indexes = {
        @Index(name = "idx_plan_name", columnList = "name"),
        @Index(name = "idx_plan_is_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Plan extends BaseEntity {

    @NotBlank(message = "Plan name is required")
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @NotNull(message = "Max products is required")
    @Min(value = 1, message = "Max products must be at least 1")
    @Column(nullable = false)
    private Integer maxProducts;

    @NotNull(message = "Max customers is required")
    @Min(value = 1, message = "Max customers must be at least 1")
    @Column(nullable = false)
    private Integer maxCustomers;

    @NotNull(message = "Max invoices is required")
    @Min(value = 1, message = "Max invoices must be at least 1")
    @Column(nullable = false)
    private Integer maxInvoices;

    @Builder.Default
    @Column(nullable = false)
    private Boolean requiredInvitedCode = false;

    @Column(nullable = true, length = 255)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

}
