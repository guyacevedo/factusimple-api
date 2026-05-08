package com.factusimple.api.creditnote.entity;

import com.factusimple.api.establishments.entity.Establishment;
import com.factusimple.api.infrastructure.persistence.BaseEntity;
import com.factusimple.api.invoice.entity.Invoice;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "credit_notes", indexes = {
    @Index(name = "idx_credit_note_invoice", columnList = "invoice_id"),
    @Index(name = "idx_credit_note_establishment", columnList = "establishment_id"),
    @Index(name = "idx_credit_note_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreditNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false, unique = true)
    private String referenceCode;

    @Column(nullable = false)
    private String correctionConceptCode;

    @Column(columnDefinition = "VARCHAR(2)")
    @Builder.Default
    private String customizationId = "20";

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CreditNoteStatus status = CreditNoteStatus.PENDING;

    private String factusNumber;

    private String cufe;

    @Column(columnDefinition = "TEXT")
    private String xmlUrl;

    @Column(columnDefinition = "TEXT")
    private String factusError;

    @OneToMany(mappedBy = "creditNote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CreditNoteItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "creditNote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CreditNotePayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "creditNote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CreditNoteAllowanceCharge> allowanceCharges = new ArrayList<>();
}
