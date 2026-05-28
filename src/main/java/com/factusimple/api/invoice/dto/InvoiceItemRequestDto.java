package com.factusimple.api.invoice.dto;

import com.factusimple.api.infrastructure.factus.codes.ProductStandardCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import com.factusimple.api.shared.dto.ItemTaxRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvoiceItemRequestDto(
    UUID productId,

    String codeReference,

    @NotBlank
    String name,

    @NotNull
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    BigDecimal quantity,

    @NotNull
    @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
    BigDecimal unitPrice,

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El descuento no puede superar 100%")
    BigDecimal discountRate,

    String unitMeasureCode,

    @ValidFactusCode(ProductStandardCode.class)
    String standardCode,

    String note,

    @NotEmpty(message = "Cada item requiere al menos un impuesto (use 0% si no aplica)")
    @Valid
    List<ItemTaxRequestDto> taxes
) {}
