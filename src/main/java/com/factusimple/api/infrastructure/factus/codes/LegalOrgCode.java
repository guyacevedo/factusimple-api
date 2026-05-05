package com.factusimple.api.infrastructure.factus.codes;

/** Tipo de organización (FACTUS). */
public enum LegalOrgCode implements FactusCode {

    PERSONA_JURIDICA("1"),
    PERSONA_NATURAL("2");

    private final String code;

    LegalOrgCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
