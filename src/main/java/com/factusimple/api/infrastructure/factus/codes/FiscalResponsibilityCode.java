package com.factusimple.api.infrastructure.factus.codes;

/** Responsabilidad fiscal (FACTUS). */
public enum FiscalResponsibilityCode implements FactusCode {

    GRAN_CONTRIBUYENTE("O-13"),
    AUTORRETENEDOR("0-15"),
    AGENTE_RETENCION_IVA("0-23"),
    REGIMEN_SIMPLE("0-47"),
    NO_RESPONSABLE("R-99-PN");

    private final String code;

    FiscalResponsibilityCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
