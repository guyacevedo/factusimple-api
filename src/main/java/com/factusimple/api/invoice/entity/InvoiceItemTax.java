package com.factusimple.api.invoice.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_item_taxes", indexes = {
        @Index(name = "idx_invoice_item_tax_item_id", columnList = "invoice_item_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "item")
public class InvoiceItemTax extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_item_id", nullable = false)
    private InvoiceItem item;

    @NotBlank
    @Column(name = "tax_code", nullable = false, length = 4)
    private String taxCode;             // impuesto (01-35) o retención (05-07)

    @NotNull
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Builder.Default
    @Column(name = "is_withholding", nullable = false)
    private Boolean isWithholding = false;
}
