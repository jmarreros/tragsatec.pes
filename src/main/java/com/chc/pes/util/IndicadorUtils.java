package com.chc.pes.util;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.chc.pes.util.ConstantUtils.*;

public final class IndicadorUtils {

    // Constantes para escala y modo de redondeo
    private static final int SCALE = 8;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    // Constructor privado para evitar instantiation
    private IndicadorUtils() {
    }

    public static BigDecimal invNormal(BigDecimal probabilidad, BigDecimal media, BigDecimal desviacion) {
        NormalDistribution normalDist = new NormalDistribution(
                media.doubleValue(),
                desviacion.doubleValue()
        );
        double inverseNormal = normalDist.inverseCumulativeProbability(probabilidad.doubleValue());

        return BigDecimal.valueOf(inverseNormal)
                .setScale(SCALE, ROUNDING);
    }

    public static BigDecimal IE_LinealMult(
            BigDecimal x,       // valor a evaluar
            BigDecimal xPre,    // umbral de prealerta
            BigDecimal xAlerta, // umbral de alerta
            BigDecimal xEmerg,  // umbral de emergencia
            BigDecimal xMin,    // mínimo absoluto
            BigDecimal xMax     // máximo absoluto
    ) {
        final BigDecimal IE_PRE = SEQUIA_IND_ESTADO_PRE;
        final BigDecimal IE_ALERTA = SEQUIA_IND_ESTADO_ALERTA;
        final BigDecimal IE_EMERG = SEQUIA_IND_ESTADO_EMERGENCIA;
        final BigDecimal IE_MIN = BigDecimal.ZERO;
        final BigDecimal IE_MAX = BigDecimal.ONE;

        if (x.compareTo(xMin) <= 0) {
            return IE_MIN;
        } else if (x.compareTo(xEmerg) <= 0) {
            // Asegurar que xEmerg no sea igual a xMin para evitar división por cero
            if (xEmerg.compareTo(xMin) == 0) {
                return IE_EMERG;
            }
            return x.subtract(xMin)
                    .multiply(IE_EMERG.subtract(IE_MIN))
                    .divide(xEmerg.subtract(xMin), SCALE, ROUNDING)
                    .add(IE_MIN);
        } else if (x.compareTo(xAlerta) <= 0) {
            if (xAlerta.compareTo(xEmerg) == 0) {
                return IE_EMERG;
            }
            return x.subtract(xEmerg)
                    .multiply(IE_ALERTA.subtract(IE_EMERG))
                    .divide(xAlerta.subtract(xEmerg), SCALE, ROUNDING)
                    .add(IE_EMERG);
        } else if (x.compareTo(xPre) <= 0) {
            if (xPre.compareTo(xAlerta) == 0) {
                return IE_ALERTA;
            }
            return x.subtract(xAlerta)
                    .multiply(IE_PRE.subtract(IE_ALERTA))
                    .divide(xPre.subtract(xAlerta), SCALE, ROUNDING)
                    .add(IE_ALERTA);
        } else if (x.compareTo(xMax) <= 0) {
            if (xMax.compareTo(xPre) == 0) {
                return IE_MAX;
            }
            return x.subtract(xPre)
                    .multiply(IE_MAX.subtract(IE_PRE))
                    .divide(xMax.subtract(xPre), SCALE, ROUNDING)
                    .add(IE_PRE);
        } else {
            return IE_MAX;
        }
    }

    public static BigDecimal IE_LinealC(BigDecimal x, BigDecimal xPre, BigDecimal xEmerg, BigDecimal xMax, BigDecimal xMin) {
        final BigDecimal IE_PRE = ESCASEZ_IND_ESTADO_PRE;
        final BigDecimal IE_EMERG = ESCASEZ_IND_ESTADO_EMERGENCIA;
        final BigDecimal IE_MAX = BigDecimal.ONE;
        final BigDecimal IE_MIN = BigDecimal.ZERO;
        BigDecimal result;

        if (x.compareTo(xMin) < 0) {
            return BigDecimal.ZERO;
        }

        if (x.compareTo(xEmerg) <= 0) { // Recta emergencia-cero
            if (x.compareTo(xMin) <= 0) { // Si x <= 0
                result = IE_MIN; // result = 0
            } else {
                result = x.subtract(xMin)
                        .multiply(IE_EMERG.subtract(IE_MIN))
                        .divide(xEmerg.subtract(xMin), SCALE, ROUNDING)
                        .add(IE_MIN);
            }
        } else if (x.compareTo(xPre) <= 0) { // Recta de prealerta-emergencia
            if (xPre.compareTo(xEmerg) == 0) {
                result = IE_EMERG;
            } else {
                result = x.subtract(xEmerg)
                        .multiply(IE_PRE.subtract(IE_EMERG))
                        .divide(xPre.subtract(xEmerg), SCALE, ROUNDING)
                        .add(IE_EMERG);
            }
        } else if (x.compareTo(xMax) <= 0) { // Recta de prealerta-Normalidad
            if (xMax.compareTo(xPre) == 0) {
                result = IE_PRE;
            } else {
                result = x.subtract(xPre)
                        .multiply(IE_MAX.subtract(IE_PRE))
                        .divide(xMax.subtract(xPre), SCALE, ROUNDING)
                        .add(IE_PRE);
            }
        } else { // Valores iguales o por encima de Xmax
            result = IE_MAX;
        }
        return result;
    }
}