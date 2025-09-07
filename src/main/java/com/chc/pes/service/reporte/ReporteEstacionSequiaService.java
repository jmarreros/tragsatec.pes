package com.chc.pes.service.reporte;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.reporte.EstadisticasMensualesSequiaDTO;
import com.chc.pes.persistence.repository.calculo.IndicadorSequiaRepository;
import com.chc.pes.util.ConstantUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteEstacionSequiaService {

    private final IndicadorSequiaRepository indicadorSequiaRepository;
    private static final int SCALE = 8; // Escala para las divisiones
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final MathContext MC = new MathContext(SCALE, ROUNDING_MODE);

    @Value("${report.max.year.sequia}")
    private Integer maxYear;

    @Autowired
    public ReporteEstacionSequiaService(IndicadorSequiaRepository indicadorSequiaRepository) {
        this.indicadorSequiaRepository = indicadorSequiaRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer estacionId, String tipoPrep) {
        // Comprobaciones maxYear a usar
        Integer maxYearToUse = java.time.Year.now().getValue();
        if (this.maxYear != null) {
            maxYearToUse = this.maxYear;
        }

        if ("prep1".equalsIgnoreCase(tipoPrep)) {
            return indicadorSequiaRepository.getAllDataIndicadorAnioMesPrep1(estacionId, maxYearToUse);
        } else if ("prep3".equalsIgnoreCase(tipoPrep)) {
            return indicadorSequiaRepository.getAllDataIndicadorAnioMesPrep3(estacionId, maxYearToUse);
        } else {
            throw new IllegalArgumentException("El tipo de precipitación especificado no es válido: " + tipoPrep);
        }
    }

    @Transactional(readOnly = true)
    public List<EstadisticasMensualesSequiaDTO> getEstadisticasMensuales(Integer estacionId, String tipoPrep) {
        List<IndicadorDataProjection> datos = getAllDataIndicadorAnioMes(estacionId, tipoPrep);

        Map<Integer, List<BigDecimal>> datosPorMes = datos.stream()
                .filter(d -> d.getDato() != null)
                .collect(Collectors.groupingBy(
                        IndicadorDataProjection::getMes,
                        Collectors.mapping(IndicadorDataProjection::getDato, Collectors.toList())
                ));

        return datosPorMes.entrySet().stream()
                .map(entry -> {
                    Integer mes = entry.getKey();
                    List<BigDecimal> valores = entry.getValue();
                    Collections.sort(valores);

                    EstadisticasMensualesSequiaDTO dto = new EstadisticasMensualesSequiaDTO();
                    dto.setMes(mes);
                    dto.setMinimo(valores.get(0));
                    dto.setMaximo(valores.get(valores.size() - 1));

                    int size = valores.size();
                    BigDecimal sum = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal media = sum.divide(new BigDecimal(size), SCALE, ROUNDING_MODE);
                    dto.setMedia(media);
                    dto.setCount(size);

                    BigDecimal mediana;
                    if (size % 2 == 0) {
                        BigDecimal mid1 = valores.get(size / 2 - 1);
                        BigDecimal mid2 = valores.get(size / 2);
                        mediana = mid1.add(mid2).divide(new BigDecimal(2), SCALE, ROUNDING_MODE);
                    } else {
                        mediana = valores.get(size / 2);
                    }
                    dto.setMediana(mediana);

                    BigDecimal desviacionEstandar;
                    if (size > 1) {
                        BigDecimal sumOfSquares = valores.stream()
                                .map(v -> v.subtract(media).pow(2))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal varianzaMuestral = sumOfSquares.divide(new BigDecimal(size - 1), SCALE, ROUNDING_MODE);
                        desviacionEstandar = new BigDecimal(Math.sqrt(varianzaMuestral.doubleValue()), MC);
                    } else {
                        desviacionEstandar = BigDecimal.ZERO;
                    }
                    dto.setDesviacionEstandar(desviacionEstandar);

                    // Cálculo de probabilidades con la inversa de la distribución normal
                    if (desviacionEstandar.compareTo(BigDecimal.ZERO) > 0) {
                        NormalDistribution normalDistribution = new NormalDistribution(media.doubleValue(), desviacionEstandar.doubleValue());

                        double probPreValue = normalDistribution.inverseCumulativeProbability(ConstantUtils.SEQUIA_PROB_ACUMULADA_PRE.doubleValue());
                        dto.setProbPre(BigDecimal.valueOf(probPreValue).round(MC));

                        double probAlertaValue = normalDistribution.inverseCumulativeProbability(ConstantUtils.SEQUIA_PROB_ACUMULADA_ALERTA.doubleValue());
                        dto.setProbAlerta(BigDecimal.valueOf(probAlertaValue).round(MC));

                        double probEmergenciaValue = normalDistribution.inverseCumulativeProbability(ConstantUtils.SEQUIA_PROB_ACUMULADA_EMERGENCIA.doubleValue());
                        dto.setProbEmergencia(BigDecimal.valueOf(probEmergenciaValue).round(MC));
                    } else {
                        dto.setProbPre(media);
                        dto.setProbAlerta(media);
                        dto.setProbEmergencia(media);
                    }

                    return dto;
                })
                .sorted((a, b) -> a.getMes().compareTo(b.getMes()))
                .collect(Collectors.toList());
    }
}