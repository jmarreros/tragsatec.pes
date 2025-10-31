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

    // Colores por escenario
    public static String getColor(Escenario escenario) {
        return switch (escenario) {
            case NORMALIDAD -> "#7FCD87"; // Verde
            case PREALERTA -> "#FFF55F"; // Amarillo
            case ALERTA -> "#FBC063"; // Naranja
            case EMERGENCIA -> "#FF755F"; // Rojo
        };
    }
}