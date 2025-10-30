package com.chc.pes.util;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtils {

    private static final Locale LOCALE_ES = new Locale("es", "ES");

    private DateUtils() {
        // Constructor privado para clase utilitaria
    }

    public static String obtenerNombreMes(Integer mes) {
        if (mes == null || mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }
        return Month.of(mes).getDisplayName(TextStyle.FULL, LOCALE_ES);
    }

    public static String obtenerNombreMesCapitalizado(Integer mes) {
        String nombreMes = obtenerNombreMes(mes);
        return nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
    }
}
