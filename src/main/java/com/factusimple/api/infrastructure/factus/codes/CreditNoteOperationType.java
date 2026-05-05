package com.factusimple.api.infrastructure.factus.codes;

/** Tipo de operación para notas de credito (FACTUS). */
public enum CreditNoteOperationType implements FactusCode {

    CON_REFERENCIA("20"),
    SIN_REFERENCIA("22");

    private final String code;

    CreditNoteOperationType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
