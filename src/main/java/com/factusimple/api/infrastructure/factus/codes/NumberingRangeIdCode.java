package com.factusimple.api.infrastructure.factus.codes;

/** Id de numeros de rango creados en factus (FACTUS). */
public enum NumberingRangeIdCode implements FactusCode {

    /// TODO: Crear en factus, se coloco 1 por defecto
    FASI("1");

    private final String code;

    NumberingRangeIdCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
