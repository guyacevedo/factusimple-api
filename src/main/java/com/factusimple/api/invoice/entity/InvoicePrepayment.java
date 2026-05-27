package com.factusimple.api.invoice.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoice_prepayments", indexes = {
        @Index(name = "idx_invoice_prepayment_invoice_id", columnList = "invoice_id")
})
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePrepayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    @NotNull
    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @NotNull
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String note;
}
