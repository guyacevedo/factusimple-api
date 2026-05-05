package com.factusimple.api.infrastructure.persistence;

/**
 * Códigos de impuestos (DIAN). No incluye códigos de retenciones (05/06/07);
 * para retenciones usar un enum aparte cuando se modele InvoiceItemTax.
 */
public enum TaxCode implements DianCode {

    IVA("01"),
    CONSUMO_DEPARTAMENTAL_NOMINAL("02"),
    INDUSTRIA_COMERCIO_AVISO("03"),
    NACIONAL_CONSUMO("04"),
    CONSUMO_DEPARTAMENTAL_PORCENTUAL("08"),
    TIMBRE("21"),
    NACIONAL_BOLSA_PLASTICA("22"),
    NACIONAL_CARBONO("23"),
    NACIONAL_COMBUSTIBLES("24"),
    CONSUMO_DATOS("30"),
    ULTRAPROCESADOS("35");

    private final String code;

    TaxCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
