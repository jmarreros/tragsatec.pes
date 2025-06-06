package tragsatec.pes.util;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class IndicadorUtils {

    // Constructor privado para evitar instanciación
    private IndicadorUtils() {
    }

    public static BigDecimal invNormal(BigDecimal probabilidad, BigDecimal media, BigDecimal desviacion) {
        NormalDistribution normalDist = new NormalDistribution(
                media.doubleValue(),
                desviacion.doubleValue()
        );
        double inverseNormal = normalDist.inverseCumulativeProbability(probabilidad.doubleValue());

        return BigDecimal.valueOf(inverseNormal)
                .setScale(8, RoundingMode.HALF_UP); // Ajustar a 8 decimales
    }

    public static BigDecimal IE_LinealMult(
            BigDecimal x,       // valor a evaluar
            BigDecimal xPre,    // umbral de prealerta
            BigDecimal xAlerta, // umbral de alerta
            BigDecimal xEmerg,  // umbral de emergencia
            BigDecimal xMin,    // mínimo absoluto
            BigDecimal xMax     // máximo absoluto
    ) {
        final BigDecimal IE_PRE = new BigDecimal("0.5");
        final BigDecimal IE_ALERTA = new BigDecimal("0.3");
        final BigDecimal IE_EMERG = new BigDecimal("0.15");
        final BigDecimal IE_MIN = BigDecimal.ZERO;
        final BigDecimal IE_MAX = BigDecimal.ONE;

        final int SCALE = 8; // Número de decimales a mantener
        final RoundingMode ROUNDING = RoundingMode.HALF_UP;

        if (x.compareTo(xMin) <= 0) {
            return IE_MIN;
        } else if (x.compareTo(xEmerg) <= 0) {
            return x.subtract(xMin)
                    .multiply(IE_EMERG.subtract(IE_MIN))
                    .divide(xEmerg.subtract(xMin), SCALE, ROUNDING)
                    .add(IE_MIN);
        } else if (x.compareTo(xAlerta) <= 0) {
            return x.subtract(xEmerg)
                    .multiply(IE_ALERTA.subtract(IE_EMERG))
                    .divide(xAlerta.subtract(xEmerg), SCALE, ROUNDING)
                    .add(IE_EMERG);
        } else if (x.compareTo(xPre) <= 0) {
            return x.subtract(xAlerta)
                    .multiply(IE_PRE.subtract(IE_ALERTA))
                    .divide(xPre.subtract(xAlerta), SCALE, ROUNDING)
                    .add(IE_ALERTA);
        } else if (x.compareTo(xMax) <= 0) {
            return x.subtract(xPre)
                    .multiply(IE_MAX.subtract(IE_PRE))
                    .divide(xMax.subtract(xPre), SCALE, ROUNDING)
                    .add(IE_PRE);
        } else {
            return IE_MAX;
        }
    }
}