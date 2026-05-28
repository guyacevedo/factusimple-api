package com.factusimple.api.infrastructure.factus.codes;

/** Id de numeros de rango creados en factus (FACTUS). */
public enum NumberingRangeIdCode implements FactusCode {

    FASI("389");

    private final String code;

    NumberingRangeIdCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
