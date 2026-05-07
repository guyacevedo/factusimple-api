package com.factusimple.api.product.dto;


import com.factusimple.api.infrastructure.factus.codes.ProductStandardCode;
import com.factusimple.api.infrastructure.factus.codes.TaxCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "El SKU es requerido")
    private String sku;

    @NotBlank(message = "El nombre del producto es requerido")
    private String name;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "El stock no puede ser negativo")
    private BigDecimal stock;

    private String unitMeasureCode;

   @ValidFactusCode(ProductStandardCode.class)
    private String standardCode;

    @ValidFactusCode(TaxCode.class)
    private String taxCode;

    private BigDecimal taxRate;

    private Boolean isActive;
}
