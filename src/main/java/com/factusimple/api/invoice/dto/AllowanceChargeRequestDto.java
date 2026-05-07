package com.factusimple.api.invoice.dto;

import com.factusimple.api.infrastructure.factus.codes.AllowanceChargeConceptCode;
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
public class AllowanceChargeRequestDto {

    @NotBlank
    @ValidFactusCode(AllowanceChargeConceptCode.class)
    private String conceptType;

    @NotNull
    private Boolean isSurcharge;        // false = descuento, true = recargo

    @NotBlank
    private String reason;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal baseAmount;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal amount;
}
