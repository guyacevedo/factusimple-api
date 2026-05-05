package com.factusimple.api.infrastructure.factus.codes;

/** Estándar de identificación del producto (FACTUS). */
public enum ProductStandardCode implements FactusCode {

    UNSPSC("001"),
    GTIN("010"),
    PARTIDA_ARANCELARIA("020"),
    ADOPCION_CONTRIBUYENTE("999");

    private final String code;

    ProductStandardCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
