package com.factusimple.api.infrastructure.factus.codes;

/** Codigos de Motivos para la generación de Notas de Ajuste (FACTUS). */
public enum ReasonsGeneratingAdjustmentNotes implements FactusCode {

    DEVOLUCION_BIENES("1"),
    ANULACION_DOCUMENTO("2"),
    DESCUENTO_PARCIAL_O_TOTAL("3"),
    AJUSTE_PRECIO("4"),
    OTROS("5");

    private final String code;

    ReasonsGeneratingAdjustmentNotes(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
