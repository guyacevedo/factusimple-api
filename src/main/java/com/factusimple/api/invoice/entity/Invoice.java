package com.factusimple.api.invoice.entity;

import com.factusimple.api.customer.entity.Customer;
import com.factusimple.api.establishment.entity.Establishment;
import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "invoices",
        indexes = {
                @Index(name = "idx_invoice_establishment_id", columnList = "establishment_id"),
                @Index(name = "idx_invoice_customer_id", columnList = "customer_id"),
                @Index(name = "idx_invoice_reference_code", columnList = "reference_code"),
                @Index(name = "idx_invoice_status", columnList = "status"),
                @Index(name = "idx_invoice_est_status", columnList = "establishment_id, status"),
                @Index(name = "idx_invoice_est_customer", columnList = "establishment_id, customer_id"),
                @Index(name = "idx_invoice_est_created", columnList = "establishment_id, created_at")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoice_reference_per_establishment",
                columnNames = {"establishment_id", "reference_code"}
        )
)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank
    @Column(name = "reference_code", nullable = false, length = 100)
    private String referenceCode;

    @Column(name = "document_type", length = 4)
    private String documentType;        // default "01"

    @Column(name = "operation_type", length = 20)
    private String operationType;       // default "10"

    @Builder.Default
    @Column(name = "send_email", nullable = false)
    private Boolean sendEmail = false;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(name = "cash_rounding", precision = 12, scale = 2)
    private BigDecimal cashRounding;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Builder.Default
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_taxes", precision = 16, scale = 2)
    private BigDecimal totalTaxes = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_discounts", precision = 16, scale = 2)
    private BigDecimal totalDiscounts = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    // Respuesta Factus (post-validación, llenado en Fase 4)
    @Column(length = 200)
    private String cufe;

    @Column(name = "xml_url", columnDefinition = "TEXT")
    private String xmlUrl;

    @Column(name = "factus_number", length = 50)
    private String factusNumber;

    @Column(name = "factus_error", columnDefinition = "TEXT")
    private String factusError;     // último error de sync con Factus (null si OK)

    @Builder.Default
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoicePayment> payments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoicePrepayment> prepayments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AllowanceCharge> allowanceCharges = new ArrayList<>();
}
