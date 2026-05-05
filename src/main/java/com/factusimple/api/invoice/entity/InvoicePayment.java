package com.factusimple.api.invoice.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoice_payments", indexes = {
        @Index(name = "idx_invoice_payment_invoice_id", columnList = "invoice_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "invoice")
public class InvoicePayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotBlank
    @Column(name = "payment_form", nullable = false, length = 1)
    private String paymentForm;             // "1" = contado, "2" = crédito

    @NotBlank
    @Column(name = "payment_method_code", nullable = false, length = 4)
    private String paymentMethodCode;

    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    @NotNull
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date")
    private LocalDate dueDate;              // requerido si paymentForm = "2"
}
