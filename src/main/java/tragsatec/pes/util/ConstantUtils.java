package tragsatec.pes.util;

import java.math.BigDecimal;

public class ConstantUtils {
    public static final BigDecimal SEQUIA_PROB_ACUMULADA_PRE = new BigDecimal("0.2");
    public static final BigDecimal SEQUIA_PROB_ACUMULADA_ALERTA = new BigDecimal("0.1");
    public static final BigDecimal SEQUIA_PROB_ACUMULADA_EMERGENCIA = new BigDecimal("0.05");

    public static final BigDecimal SEQUIA_IND_ESTADO_PRE = new BigDecimal("0.5");
    public static final BigDecimal SEQUIA_IND_ESTADO_ALERTA = new BigDecimal("0.3");
    public static final BigDecimal SEQUIA_IND_ESTADO_EMERGENCIA = new BigDecimal("0.15");

    public static final BigDecimal ESCASEZ_IND_ESTADO_PRE = new BigDecimal("0.5");
    public static final BigDecimal ESCASEZ_IND_ESTADO_ALERTA = new BigDecimal("0.3");
    public static final BigDecimal ESCASEZ_IND_ESTADO_EMERGENCIA = new BigDecimal("0.15");

    // Deben coincidir con los valores generados por `escenario + estadistico` en la consulta.
    public static final String ESCASEZ_FACTOR_XPRE = "XPRE"; // EstrésMínimo
    public static final String ESCASEZ_FACTOR_XMAX = "XMAX"; // NormalidadMínimo
    public static final String ESCASEZ_FACTOR_XEMERG = "XEMERG"; // NormalidadMáximo
    public static final String ESCASEZ_FACTOR_XMIN = "XMIN"; // Caso E_EBRO, E_ULLIVARRIURRUNAGA_EBRO
}