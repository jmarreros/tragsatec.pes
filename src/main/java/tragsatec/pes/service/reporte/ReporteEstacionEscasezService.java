package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.reporte.EstadisticasMensualesDTO;
import tragsatec.pes.persistence.repository.calculo.IndicadorEscasezRepository;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteEstacionEscasezService {

    private final IndicadorEscasezRepository indicadorEscasezRepository;
    private static final int SCALE = 8;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final MathContext MC = new MathContext(SCALE, ROUNDING_MODE);

    @Autowired
    public ReporteEstacionEscasezService(IndicadorEscasezRepository indicadorEscasezRepository) {
        this.indicadorEscasezRepository = indicadorEscasezRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer estacionId) {
        return indicadorEscasezRepository.getAllDataIndicadorAnioMes(estacionId);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasMensualesDTO> getEstadisticasMensuales(Integer estacionId) {
        List<IndicadorDataProjection> datos = getAllDataIndicadorAnioMes(estacionId);

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

                    EstadisticasMensualesDTO dto = new EstadisticasMensualesDTO();
                    dto.setMes(mes);
                    dto.setMinimo(valores.get(0));
                    dto.setMaximo(valores.get(valores.size() - 1));

                    int size = valores.size();
                    BigDecimal sum = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal media = sum.divide(new BigDecimal(size), SCALE, ROUNDING_MODE);
                    dto.setMedia(media);

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

                    return dto;
                })
                .sorted((a, b) -> a.getMes().compareTo(b.getMes()))
                .collect(Collectors.toList());
    }
}