package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.reporte.EstadisticasMensualesDTO;
import tragsatec.pes.persistence.repository.calculo.IndicadorSequiaRepository;

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

    @Autowired
    public ReporteEstacionSequiaService(IndicadorSequiaRepository indicadorSequiaRepository) {
        this.indicadorSequiaRepository = indicadorSequiaRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer estacionId, String tipoPrep) {
        if ("prep1".equalsIgnoreCase(tipoPrep)) {
            return indicadorSequiaRepository.getAllDataIndicadorAnioMesPrep1(estacionId);
        } else if ("prep3".equalsIgnoreCase(tipoPrep)) {
            return indicadorSequiaRepository.getAllDataIndicadorAnioMesPrep3(estacionId);
        } else {
            throw new IllegalArgumentException("El tipo de precipitación especificado no es válido: " + tipoPrep);
        }
    }

    @Transactional(readOnly = true)
    public List<EstadisticasMensualesDTO> getEstadisticasMensuales(Integer estacionId, String tipoPrep) {
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

                    EstadisticasMensualesDTO dto = new EstadisticasMensualesDTO();

                    dto.setMes(mes); //mes
                    dto.setMinimo(valores.get(0)); // mínimo
                    dto.setMaximo(valores.get(valores.size() - 1)); // máximo

                    BigDecimal sum = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal media = sum.divide(new BigDecimal(valores.size()), SCALE, ROUNDING_MODE);
                    dto.setMedia(media); // media

                    BigDecimal mediana;
                    int size = valores.size();
                    if (size % 2 == 0) {
                        BigDecimal mid1 = valores.get(size / 2 - 1);
                        BigDecimal mid2 = valores.get(size / 2);
                        mediana = mid1.add(mid2).divide(new BigDecimal(2), SCALE, ROUNDING_MODE);
                    } else {
                        mediana = valores.get(size / 2);
                    }
                    dto.setMediana(mediana); // mediana

                    // desviación estándar
                    if (size > 1) {
                        BigDecimal sumOfSquares = valores.stream()
                                .map(v -> v.subtract(media).pow(2))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal varianzaMuestral = sumOfSquares.divide(new BigDecimal(size - 1), SCALE, ROUNDING_MODE);

                        BigDecimal desviacionEstandar = new BigDecimal(Math.sqrt(varianzaMuestral.doubleValue()), MC);
                        dto.setDesviacionEstandar(desviacionEstandar);
                    } else {
                        // La desviación estándar de una muestra con 0 o 1 elementos es 0.
                        dto.setDesviacionEstandar(BigDecimal.ZERO);
                    }

                    return dto;
                })
                .sorted((a, b) -> a.getMes().compareTo(b.getMes()))
                .collect(Collectors.toList());
    }
}