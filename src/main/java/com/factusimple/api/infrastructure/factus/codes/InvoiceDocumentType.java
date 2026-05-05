package com.factusimple.api.infrastructure.factus.codes;

/** Tipo de documento para la factura (FACTUS). */
public enum InvoiceDocumentType implements FactusCode {

    FACTURA_VENTA("01"),
    INSTRUMENTO_TRANSMISION("03");

    private final String code;

    InvoiceDocumentType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
