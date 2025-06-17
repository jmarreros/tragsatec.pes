package tragsatec.pes.util;

import java.math.BigDecimal;

public class ConstantUtils {
    public static final BigDecimal FACTOR_PRE_ALERTA = new BigDecimal("0.2");
    public static final BigDecimal FACTOR_ALERTA = new BigDecimal("0.1");
    public static final BigDecimal FACTOR_EMERGENCIA = new BigDecimal("0.05");


    // Deben coincidir con los valores generados por `escenario + estadistico` en la consulta.
    public static final String FACTOR_XPRE = "XPRE"; // EstrésMínimo
    public static final String FACTOR_XMAX = "XMAX"; // NormalidadMínimo
    public static final String FACTOR_XEMERG = "XEMERG"; // NormalidadMáximo
    public static final String FACTOR_XMIN = "XMIN"; // Caso E_EBRO, E_ULLIVARRIURRUNAGA_EBRO
}