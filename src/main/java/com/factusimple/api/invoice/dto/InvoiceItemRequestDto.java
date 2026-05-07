package com.factusimple.api.invoice.dto;

import com.factusimple.api.infrastructure.factus.codes.ProductStandardCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemRequestDto {

    private UUID productId;

    private String codeReference;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El descuento no puede superar 100%")
    private BigDecimal discountRate;

    private String unitMeasureCode;

    @ValidFactusCode(ProductStandardCode.class)
    private String standardCode;

    private String note;

    @NotEmpty(message = "Cada item requiere al menos un impuesto (use 0% si no aplica)")
    @Valid
    private List<InvoiceItemTaxRequestDto> taxes;
}
