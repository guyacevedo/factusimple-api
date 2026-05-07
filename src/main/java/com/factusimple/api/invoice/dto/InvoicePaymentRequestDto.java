package com.factusimple.api.invoice.dto;

import com.factusimple.api.infrastructure.factus.codes.PaymentFormCode;
import com.factusimple.api.infrastructure.factus.codes.PaymentMethodCode;
import com.factusimple.api.infrastructure.factus.validation.ValidFactusCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentRequestDto {

    @NotBlank
    @ValidFactusCode(PaymentFormCode.class)
    private String paymentForm;

    @NotBlank
    @ValidFactusCode(PaymentMethodCode.class)
    private String paymentMethodCode;

    private String referenceCode;

    @NotNull
    @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
    private BigDecimal amount;

    private LocalDate dueDate;          // requerido si paymentForm = "2" (validado en service)
}
