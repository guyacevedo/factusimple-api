package com.factusimple.api.creditnote.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_note_allowance_charges", indexes = {
    @Index(name = "idx_credit_note_allowance_charge_credit_note", columnList = "credit_note_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreditNoteAllowanceCharge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_note_id", nullable = false)
    private CreditNote creditNote;

    @Column(nullable = false)
    private String conceptType;

    @Column(nullable = false)
    @Builder.Default
    private boolean isSurcharge = false;

    private String reason;

    @Column(precision = 19, scale = 2)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}
