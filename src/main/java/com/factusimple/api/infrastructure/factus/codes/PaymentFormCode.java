package com.factusimple.api.infrastructure.factus.codes;

/** Forma de pago (FACTUS). */
public enum PaymentFormCode implements FactusCode {

    CONTADO("1"),
    CREDITO("2");

    private final String code;

    PaymentFormCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
