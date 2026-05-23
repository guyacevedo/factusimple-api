package com.factusimple.api.infrastructure.factus.codes;

/** Tipo de operación para la factura (FACTUS). */
public enum InvoiceOperationType implements FactusCode {

    ESTANDAR("10"),
    MANDATOS("11"),
    TRANSPORTE("12");

    private final String code;

    InvoiceOperationType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
