package com.factusimple.api.infrastructure.factus.codes;

/** Método de pago (FACTUS). */
public enum PaymentMethodCode implements FactusCode {

    NO_DEFINIDO("1"),
    EFECTIVO("10"),
    CHEQUE("20"),
    CONSIGNACION("42"),
    TRANSFERENCIA("47"),
    TARJETA_CREDITO("48"),
    TARJETA_DEBITO("49"),
    BONOS("71"),
    VALES("72"),
    OTRO("ZZZ");

    private final String code;

    PaymentMethodCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
