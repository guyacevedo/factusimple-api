package com.factusimple.api.product.entity;

import com.factusimple.api.establishments.entity.Establishment;
import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_establishment_id", columnList = "establishment_id"),
                @Index(name = "idx_product_sku", columnList = "sku")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_sku_per_establishment",
                columnNames = {"establishment_id", "sku"}
        )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @NotBlank(message = "El SKU es requerido")
    @Column(nullable = false, length = 50)
    private String sku;

    @NotBlank(message = "El nombre del producto es requerido")
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal stock;

    @Column(name = "unit_measure_code", length = 10)
    private String unitMeasureCode;

    @Column(name = "standard_code", length = 10)
    private String standardCode;

    @Column(name = "tax_code", length = 4)
    private String taxCode;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
