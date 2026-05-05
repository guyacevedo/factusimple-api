package com.factusimple.api.infrastructure.factus.codes;

/** Tipo de documento para los rangos de numeración (FACTUS). */
public enum NumeringRangesDocumentType implements FactusCode {

    FACTURA_VENTA("21"),
    NOTA_CREDITO("22"),
    NOTA_DEBITO("23"),
    DOCUMENTO_SOPORTE("24"),
    NOTA_AJUSTE_DOCUMETO_SOPORTE("25"),
    NOMINA("26"),
    NOTA_AJUSTE_NOMINA("27"),
    NOTA_ELIMINACION_NOMINA("28"),
    FACTURA_TALONARIO_PAPEL("30");

    private final String code;

    NumeringRangesDocumentType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
