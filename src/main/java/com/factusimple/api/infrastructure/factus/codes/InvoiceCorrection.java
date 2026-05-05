package com.factusimple.api.infrastructure.factus.codes;

/** Codigos de correcion para la factura (FACTUS). */
public enum InvoiceCorrection implements FactusCode {

    DEVOLUCION_BIENES("1"),
    ANULACION_FACTURA("2"),
    DESCUENTO_PARCIAL_O_TOTAL("3"),
    AJUSTE_PRECIO("4"),
    DESCUENTO_COMERCIAL_PAGO("5"),
    DESCUENTO_COMERCIAL_VENTAS("6");

    private final String code;

    InvoiceCorrection(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
