package com.factusimple.api.infrastructure.factus.codes;

/** Códigos de concepto para descuentos y recargos (FACTUS). */
public enum AllowanceChargeConceptCode implements FactusCode {

    RECARGO_CONDICIONADO("03");

    private final String code;

    AllowanceChargeConceptCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
