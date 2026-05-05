package com.factusimple.api.infrastructure.factus.codes;

/** Tipos de documentos de identidad (FACTUS). */
public enum IdentityDocumentType implements FactusCode {

    REGISTRO_CIVIL("11"),
    TARJETA_IDENTIDAD("12"),
    CEDULA_CIUDADANIA("13"),
    TARJETA_EXTRANJERIA("21"),
    CEDULA_EXTRANJERIA("22"),
    NIT("31"),
    PASAPORTE("41"),
    DOC_IDENTIFICACION_EXTRANJERO("42"),
    PEP("47"),
    PPT("48"),
    NIT_OTRO_PAIS("50"),
    NUIP("91");

    private final String code;

    IdentityDocumentType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
