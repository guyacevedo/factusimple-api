package com.factusimple.api.infrastructure.factus.validation;

import com.factusimple.api.infrastructure.factus.codes.FactusCode;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valida que un String sea uno de los códigos definidos por un enum
 * que implemente {@link FactusCode}. null se considera válido (use @NotBlank
 * si el campo es obligatorio).
 */
@Documented
@Constraint(validatedBy = FactusCodeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFactusCode {

    Class<? extends FactusCode> value();

    String message() default "Código FACTUS inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
