package com.factusimple.api.creditnote.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_note_item_taxes", indexes = {
    @Index(name = "idx_credit_note_item_tax_item", columnList = "item_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreditNoteItemTax extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private CreditNoteItem item;

    @Column(nullable = false)
    private String taxCode;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(nullable = false)
    @Builder.Default
    private boolean isWithholding = false;
}
