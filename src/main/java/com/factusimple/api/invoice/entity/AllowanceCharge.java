package com.factusimple.api.invoice.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "allowance_charges", indexes = {
        @Index(name = "idx_allowance_charge_invoice_id", columnList = "invoice_id")
})
    @Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowanceCharge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotBlank
    @Column(name = "concept_type", nullable = false, length = 4)
    private String conceptType;         // 00, 01, 02, 03

    @NotNull
    @Column(name = "is_surcharge", nullable = false)
    private Boolean isSurcharge;        // false = descuento, true = recargo

    @NotBlank
    @Column(nullable = false, length = 255)
    private String reason;

    @NotNull
    @Column(name = "base_amount", nullable = false, precision = 16, scale = 2)
    private BigDecimal baseAmount;

    @NotNull
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal amount;
}
