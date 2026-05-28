package com.factusimple.api.invoice.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import com.factusimple.api.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice_items", indexes = {
        @Index(name = "idx_invoice_item_invoice_id", columnList = "invoice_id"),
        @Index(name = "idx_invoice_item_product_id", columnList = "product_id")
})
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;            // nullable: items sin producto predefinido

    @Column(name = "code_reference", length = 50)
    private String codeReference;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;       // sin impuestos

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;    // porcentaje 0-100

    @Column(name = "unit_measure_code", length = 10)
    private String unitMeasureCode;

    @Column(name = "standard_code", length = 10)
    private String standardCode;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Builder.Default
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItemTax> taxes = new ArrayList<>();
}
