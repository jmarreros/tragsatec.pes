package com.chc.pes.service.reporte;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.chc.pes.util.ConstantUtils.*;

@Service
public class ReporteConstantesService {
    @Value("${report.max.year.sequia}")
    private Integer maxYearSequia;

    @Value("${report.max.year.escasez}")
    private Integer maxYearEscasez;


    public Map<String, Object> getConstantesDeSequia() {
        Map<String, Object> constantes = new LinkedHashMap<>();

        constantes.put("SEQUIA_PROB_ACUMULADA_PRE", SEQUIA_PROB_ACUMULADA_PRE);
        constantes.put("SEQUIA_PROB_ACUMULADA_ALERTA", SEQUIA_PROB_ACUMULADA_ALERTA);
        constantes.put("SEQUIA_PROB_ACUMULADA_EMERGENCIA", SEQUIA_PROB_ACUMULADA_EMERGENCIA);

        constantes.put("SEQUIA_IND_ESTADO_PRE", SEQUIA_IND_ESTADO_PRE);
        constantes.put("SEQUIA_IND_ESTADO_ALERTA", SEQUIA_IND_ESTADO_ALERTA);
        constantes.put("SEQUIA_IND_ESTADO_EMERGENCIA", SEQUIA_IND_ESTADO_EMERGENCIA);

        constantes.put("ESTADISTICA_MAX_YEAR_SEQUIA", maxYearSequia);
        return constantes;
    }

    public Map<String, Object> getConstantesDeEscasez() {
        Map<String, Object> constantes = new LinkedHashMap<>();

        constantes.put("ESCASEZ_IND_ESTADO_PRE", ESCASEZ_IND_ESTADO_PRE);
        constantes.put("ESCASEZ_IND_ESTADO_ALERTA", ESCASEZ_IND_ESTADO_ALERTA);
        constantes.put("ESCASEZ_IND_ESTADO_EMERGENCIA", ESCASEZ_IND_ESTADO_EMERGENCIA);

        constantes.put("ESCASEZ_FACTOR_XPRE", ESCASEZ_FACTOR_XPRE);
        constantes.put("ESCASEZ_FACTOR_XMAX", ESCASEZ_FACTOR_XMAX);
        constantes.put("ESCASEZ_FACTOR_XEMERG", ESCASEZ_FACTOR_XEMERG);
        constantes.put("ESCASEZ_FACTOR_XMIN", ESCASEZ_FACTOR_XMIN);

        constantes.put("ESTADISTICA_MAX_YEAR_ESCASEZ", maxYearEscasez);

        return constantes;
    }
}