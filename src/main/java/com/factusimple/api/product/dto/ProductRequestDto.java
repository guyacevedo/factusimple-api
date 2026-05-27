package com.factusimple.api.product.dto;


import com.factusimple.api.infrastructure.factus.codes.ProductStandardCode;
import com.factusimple.api.infrastructure.factus.codes.TaxCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequestDto(
    @NotBlank(message = "El SKU es requerido")
    String sku,

    @NotBlank(message = "El nombre del producto es requerido")
    String name,

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    BigDecimal price,

    @DecimalMin(value = "0.0", inclusive = true, message = "El stock no puede ser negativo")
    BigDecimal stock,

    String unitMeasureCode,

    @ValidFactusCode(ProductStandardCode.class)
    String standardCode,

    @ValidFactusCode(TaxCode.class)
    String taxCode,

    BigDecimal taxRate,

    Boolean isActive
) {}
