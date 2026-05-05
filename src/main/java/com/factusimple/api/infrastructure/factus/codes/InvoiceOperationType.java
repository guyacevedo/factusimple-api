package com.factusimple.api.infrastructure.factus.codes;

/** Tipo de operación para la factura (FACTUS). */
public enum InvoiceOperationType implements FactusCode {

    ESTANDAR("10"),
    MANDATOS("11"),
    TRANSPORTE("12"),
    SS_CUFE("SS-CUFE"),
    SS_REPORTE("SS-Reporte"),
    SS_SIN_APORTE("SS-SinAporte"),
    SS_RECAUDO("SS-Recaudo");

    private final String code;

    InvoiceOperationType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
