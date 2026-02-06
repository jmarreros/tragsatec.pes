package com.chc.pes.service.reporte;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.estructura.UmbralEscasezDataProjection;
import com.chc.pes.dto.reporte.EstadisticasMensualesEscasezDTO;
import com.chc.pes.persistence.repository.calculo.IndicadorEscasezRepository;
import com.chc.pes.service.estructura.PesUmbralEscasezService;
import com.chc.pes.util.ConstantUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReporteEstacionEscasezService {

    private final IndicadorEscasezRepository indicadorEscasezRepository;
    private final PesUmbralEscasezService pesUmbralEscasezService;
    private static final int SCALE = 8;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final MathContext MC = new MathContext(SCALE, ROUNDING_MODE);

    @Value("${report.max.year.escasez}")
    private Integer maxYear;

    @Autowired
    public ReporteEstacionEscasezService(IndicadorEscasezRepository indicadorEscasezRepository, PesUmbralEscasezService pesUmbralEscasezService) {
        this.indicadorEscasezRepository = indicadorEscasezRepository;
        this.pesUmbralEscasezService = pesUmbralEscasezService;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer estacionId) {
        Integer maxYearToUse = java.time.Year.now().getValue();

        return indicadorEscasezRepository.getAllDataIndicadorAnioMes(estacionId, maxYearToUse);
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMesEstadisticas(Integer estacionId) {
        // Comprobaciones maxYear a usar
        Integer maxYearToUse = java.time.Year.now().getValue();
        if (this.maxYear != null) {
            maxYearToUse = this.maxYear;
        }

        List<IndicadorDataProjection> result = indicadorEscasezRepository.getAllDataIndicadorAnioMes(estacionId, maxYearToUse);

        // Quitar los tres últimos valores del último año (mes 10, 11, 12) para tener el año hidrológico
        if (!result.isEmpty()) {
            int lastYear = result.get(result.size() - 1).getAnio();
            result = result.stream()
                    .filter(d -> !(d.getAnio() == lastYear && d.getMes() >= 10))
                    .collect(Collectors.toList());
        } else {
            result = Collections.emptyList();
        }
        return result;

    }

    @Transactional(readOnly = true)
    public List<EstadisticasMensualesEscasezDTO> getEstadisticasMensuales(Integer estacionId) {
        List<IndicadorDataProjection> datos = getAllDataIndicadorAnioMesEstadisticas(estacionId);
        List<UmbralEscasezDataProjection> umbrales = pesUmbralEscasezService.findUmbralesByEstacionIdAndCurrentPesId(estacionId);

        Map<Integer, Map<String, BigDecimal>> umbralesPorMes = new HashMap<>();
        for (UmbralEscasezDataProjection umbral : umbrales) {
            String param = umbral.getParam();
            List<Function<UmbralEscasezDataProjection, BigDecimal>> getters = Arrays.asList(
                    p -> p.getMes_1(), p -> p.getMes_2(), p -> p.getMes_3(),
                    p -> p.getMes_4(), p -> p.getMes_5(), p -> p.getMes_6(),
                    p -> p.getMes_7(), p -> p.getMes_8(), p -> p.getMes_9(),
                    p -> p.getMes_10(), p -> p.getMes_11(), p -> p.getMes_12()
            );
            for (int i = 0; i < getters.size(); i++) {
                int mes = i + 1;
                umbralesPorMes.computeIfAbsent(mes, k -> new HashMap<>()).put(param, getters.get(i).apply(umbral));
            }
        }

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

                    EstadisticasMensualesEscasezDTO dto = new EstadisticasMensualesEscasezDTO();
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
                        desviacionEstandar = BigDecimal.valueOf(Math.sqrt(varianzaMuestral.doubleValue())).round(MC);
                    } else {
                        desviacionEstandar = BigDecimal.ZERO;
                    }
                    dto.setDesviacionEstandar(desviacionEstandar);


                    Map<String, BigDecimal> umbralesDelMes = umbralesPorMes.getOrDefault(mes, Collections.emptyMap());
                    BigDecimal xpre = umbralesDelMes.get(ConstantUtils.ESCASEZ_FACTOR_XPRE);
                    BigDecimal xemerg = umbralesDelMes.get(ConstantUtils.ESCASEZ_FACTOR_XEMERG);
                    BigDecimal xAlerta = umbralesDelMes.get(ConstantUtils.ESCASEZ_FACTOR_XALERTA);
                    BigDecimal xMax = umbralesDelMes.get(ConstantUtils.ESCASEZ_FACTOR_XMAX);
                    BigDecimal xMin = umbralesDelMes.get(ConstantUtils.ESCASEZ_FACTOR_XMIN);

                    // Calcular XALERTA
                    if (xAlerta==null && xpre != null && xemerg != null) {
                        BigDecimal numerador = ConstantUtils.ESCASEZ_IND_ESTADO_ALERTA.subtract(ConstantUtils.ESCASEZ_IND_ESTADO_EMERGENCIA)
                                .multiply(xpre.subtract(xemerg));
                        BigDecimal denominador = ConstantUtils.ESCASEZ_IND_ESTADO_PRE.subtract(ConstantUtils.ESCASEZ_IND_ESTADO_EMERGENCIA);

                        if (denominador.compareTo(BigDecimal.ZERO) != 0) {
                            xAlerta = numerador.divide(denominador, SCALE, ROUNDING_MODE).add(xemerg);
                        }
                    }

                    dto.setXemerg(xemerg);
                    dto.setXpre(xpre);
                    dto.setXmax(xMax);
                    dto.setXmin(xMin);
                    dto.setXalerta(xAlerta);

                    // Calcular ocurrencias
                    if (desviacionEstandar.compareTo(BigDecimal.ZERO) > 0) {
                        NormalDistribution dist = new NormalDistribution(media.doubleValue(), desviacionEstandar.doubleValue());

                        if (xpre != null) {
                            dto.setOcurrenciaPre(BigDecimal.valueOf(dist.cumulativeProbability(xpre.doubleValue())));
                        }
                        if (dto.getXalerta() != null) {
                            dto.setOcurrenciaAlerta(BigDecimal.valueOf(dist.cumulativeProbability(dto.getXalerta().doubleValue())));
                        }
                        if (xemerg != null) {
                            dto.setOcurrenciaEmergencia(BigDecimal.valueOf(dist.cumulativeProbability(xemerg.doubleValue())));
                        }
                    }

                    return dto;
                })
                .sorted(Comparator.comparing(EstadisticasMensualesEscasezDTO::getMes))
                .collect(Collectors.toList());
    }
}