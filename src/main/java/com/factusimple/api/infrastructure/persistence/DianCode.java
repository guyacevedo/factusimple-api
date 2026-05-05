package com.factusimple.api.infrastructure.persistence;

/**
 * Marca a un enum como portador de un código DIAN/Factus.
 * Cada constante expone su código string oficial (ej. "O-13", "01", "999").
 */
public interface DianCode {
    String getCode();
}
