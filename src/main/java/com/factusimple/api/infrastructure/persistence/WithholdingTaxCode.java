package com.factusimple.api.infrastructure.persistence;

/** Códigos de retenciones (DIAN). Usar cuando InvoiceItemTax.isWithholding = true. */
public enum WithholdingTaxCode implements DianCode {

    RETE_IVA("05"),
    RETE_RENTA("06"),
    RETE_ICA("07");

    private final String code;

    WithholdingTaxCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
