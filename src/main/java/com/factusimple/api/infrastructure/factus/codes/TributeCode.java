package com.factusimple.api.infrastructure.factus.codes;

/** Tributo del cliente (FACTUS). */
public enum TributeCode implements FactusCode {

    IVA("01"),
    NO_APLICA("ZZ");

    private final String code;

    TributeCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
