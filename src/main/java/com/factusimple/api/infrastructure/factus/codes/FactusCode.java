package com.factusimple.api.infrastructure.factus.codes;

/**
 * Marca a un enum como portador de un código DIAN/FACTUS.
 * Cada constante expone su código string oficial (ej. "O-13", "01", "999").
 */
public interface FactusCode {
    String getCode();
}
