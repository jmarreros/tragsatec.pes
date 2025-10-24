package com.chc.pes.util;


import java.util.Arrays;

public enum Escenario {
    NORMALIDAD("normalidad"),
    PREALERTA("prealerta"),
    ALERTA("alerta"),
    EMERGENCIA("emergencia");

    private final String value;

    Escenario(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Escenario fromValue(String value) {
        return Arrays.stream(values())
                .filter(escenario -> escenario.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(NORMALIDAD);
    }
}