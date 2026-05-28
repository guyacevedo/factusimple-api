package com.factusimple.api.infrastructure.factus.validation;

import com.factusimple.api.infrastructure.factus.codes.FactusCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FactusCodeValidator implements ConstraintValidator<ValidFactusCode, String> {

    private Set<String> validCodes;
    private String validCodesCsv;

    @Override
    public void initialize(ValidFactusCode constraint) {
        Class<? extends FactusCode> clazz = constraint.value();
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException(
                    "El target de @ValidFactusCode debe ser un enum: " + clazz.getName());
        }
        validCodes = Arrays.stream(clazz.getEnumConstants())
                .map(FactusCode::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        validCodesCsv = String.join(", ", validCodes);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (validCodes.contains(value)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                "Código '" + value + "' no es válido. Valores aceptados: " + validCodesCsv)
                .addConstraintViolation();
        return false;
    }
}
