package com.factusimple.api.infrastructure.factus.codes;

/** Tipos de documentos de identidad (FACTUS). */
public enum DocumentsAndNotesIdentityDocumentType implements FactusCode {

    TARJETA_EXTRANJERIA("4"),
    CEDULA_EXTRANJERIA("5"),
    NIT("6"),
    PASAPORTE("7"),
    DOC_IDENTIFICACION_EXTRANJERO("8"),
    PEP("9"),
    NIT_OTRO_PAIS("10");

    private final String code;

    DocumentsAndNotesIdentityDocumentType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
