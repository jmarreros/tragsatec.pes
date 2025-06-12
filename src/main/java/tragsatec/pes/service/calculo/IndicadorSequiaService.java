package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.AcumuladoSequiaDTO;
import tragsatec.pes.dto.estructura.PesUmbralSequiaDTO;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.dto.medicion.MedicionDTO;
import tragsatec.pes.exception.CalculoIndicadorException;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorSequiaRepository;
import tragsatec.pes.service.estructura.PesUmbralSequiaService;
import tragsatec.pes.service.medicion.DetalleMedicionService;
import tragsatec.pes.service.medicion.MedicionService;
import tragsatec.pes.util.IndicadorUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tragsatec.pes.util.ConstantUtils.*;

@Service
@RequiredArgsConstructor
public class IndicadorSequiaService {

    private final IndicadorSequiaRepository repository;
    private final MedicionService medicionService;
    private final DetalleMedicionService detalleMedicionService;
    private final PesUmbralSequiaService pesUmbralSequiaService;

    @Transactional
    public void calcularIndicadorSequia() {
        // 1- Revisar la tabla de mediciones para obtener la medición de sequia pendiente de calcular (Tipo 'S')
        MedicionDTO medicion = medicionService.findFirstNotProcessedMedicionByTipo('S');
        if (medicion == null) {
            throw new CalculoIndicadorException("No hay mediciones de sequía pendientes de calcular.");
        }

        Integer medicionId = medicion.getId();
        Byte mes = medicion.getMes();
        Integer pesId = medicion.getPesId();

        // 2- Obtener el detalle de la medicion de la tabla de detalles de mediciones por Id
        List<DetalleMedicionDTO> detallesMedicion = detalleMedicionService.findByMedicionId(medicionId);
        if (detallesMedicion.isEmpty()) {
            throw new CalculoIndicadorException("No hay detalles de medición para la medición ID: " + medicionId);
        }

        //3- Borrar los indicadores de sequía si existen para la medición actual y volver a calcularlos
        repository.deleteByMedicionId(medicionId);

        //4- Obtener todos los umbrales para el PES y mes actual de una vez
        List<PesUmbralSequiaDTO> umbralesSequia = pesUmbralSequiaService.findByPesIdAndMes(pesId, mes);
        if (umbralesSequia.isEmpty()) {
            throw new CalculoIndicadorException("No se encontraron umbrales de sequía para PES ID: " + pesId + " y mes: " + mes);
        }

        //5- Para los acumulados, obtener los registros del mes anterior
        List<AcumuladoSequiaDTO> acumuladosMesAnterior = getAcumuladosMesAnterior(detallesMedicion);

        // 6- Recorrer el detalle de la medición y calcular los indicadores de sequía para cada estación
        for (DetalleMedicionDTO itemDetalleMedicion : detallesMedicion) {
            // 6.1- Obtener el ID y valor de la estación desde el detalle de medición
            Integer estacionId = itemDetalleMedicion.getEstacionId();
            BigDecimal valorPre1 = itemDetalleMedicion.getValor();

            // 6.2- Buscar el umbral para la estación actual en la lista cargada
            PesUmbralSequiaDTO umbralEstacionActual = umbralesSequia.stream()
                    .filter(u -> u.getEstacionId().equals(estacionId))
                    .findFirst()
                    .orElseThrow(() -> new CalculoIndicadorException("No se encontraron umbrales de sequía para PES ID: " + pesId + ", estación: " + estacionId + ", mes: " + mes));

            // 6.3- Acumulados de valores de precipitación y el índice de sequía
            AcumuladoSequiaDTO acumulados = acumuladosMesAnterior.stream()
                    .filter(a -> a.getEstacionId().equals(estacionId))
                    .findFirst()
                    .orElse(new AcumuladoSequiaDTO(estacionId, BigDecimal.ZERO, BigDecimal.ZERO));

            // 6.4- Calcular el indicador de sequía para la estación, año y mes específicos
            BigDecimal valorIndice1 = calcularIndicadorSequia(valorPre1, umbralEstacionActual, 1);
            BigDecimal valorPrep3 = acumulados.getPre3().add(valorPre1);
            BigDecimal valorIndice3 = calcularIndicadorSequia(valorPrep3, umbralEstacionActual, 3);
            BigDecimal valorPrep6 = acumulados.getPre6().add(valorPre1);
            BigDecimal valorIndice6 = calcularIndicadorSequia(valorPrep6, umbralEstacionActual, 6);

            // 6.5- Construir el objeto IndicadorSequiaEntity con los valores calculados
            IndicadorSequiaEntity indicadorAGuardar = new IndicadorSequiaEntity();
            indicadorAGuardar.setMedicionId(medicion.getId());
            indicadorAGuardar.setMes(medicion.getMes());
            indicadorAGuardar.setAnio(medicion.getAnio());
            indicadorAGuardar.setEstacionId(estacionId);
            indicadorAGuardar.setPrep1(valorPre1);
            indicadorAGuardar.setIeB1(valorIndice1);
            indicadorAGuardar.setPrep3(valorPrep3);
            indicadorAGuardar.setIeB3(valorIndice3);
            indicadorAGuardar.setPrep6(valorPrep6);
            indicadorAGuardar.setIeB6(valorIndice6);

            // 6.6- Guardar el resultado del indicador de sequía en la base de datos
            Long indicadorSequiaId = saveIndicadorSequia(indicadorAGuardar);
            if (indicadorSequiaId == null) {
                throw new CalculoIndicadorException("Error al guardar el indicador de sequía para la estación ID: " + estacionId);
            }
        }

        // 7- Marcar la medición como procesada si fue correcto
        medicionService.marcarComoProcesada(medicionId);
    }

    // Obtener los acumulados de meses anteriores para las estaciones
    private List<AcumuladoSequiaDTO> getAcumuladosMesAnterior(List<DetalleMedicionDTO> detallesMedicion) {
        // 1. Usar un Map para construir y combinar los DTOs. La clave será el estacionId.
        // Inicializar el mapa con todas las estaciones de detallesMedicion, con valores acumulados en CERO.
        Map<Integer, AcumuladoSequiaDTO> mapaAcumulados = detallesMedicion.stream()
                .collect(Collectors.toMap(
                        DetalleMedicionDTO::getEstacionId,
                        detalle -> new AcumuladoSequiaDTO(detalle.getEstacionId(), BigDecimal.ZERO, BigDecimal.ZERO),
                        (existente, nuevo) -> existente // En caso de duplicados en detallesMedicion (poco probable)
                ));

        // 2. Obtener los datos de acumulados para 2 meses
        // La consulta devuelve List<Object[]>, donde Object[0] es estacion_id (Integer)
        // y Object[1] es la suma de prep1 (BigDecimal).
        List<Object[]> acumulados2MesesRaw = repository.sumLastNPrep1ForEachEstacion(2);

        // Procesar los resultados de 2 meses
        for (Object[] row : acumulados2MesesRaw) {
            Integer estacionId = (Integer) row[0];
            BigDecimal pre3 = (row[1] instanceof BigDecimal) ? (BigDecimal) row[1] : BigDecimal.ZERO;

            // Obtener el DTO del mapa
            AcumuladoSequiaDTO dto = mapaAcumulados.get(estacionId);
            if (dto != null) {
                dto.setPre3(pre3);
            }
        }

        // 3. Obtener los datos de acumulados para 5 meses
        // La consulta devuelve List<Object[]>, donde Object[0] es estacion_id (Integer)
        // y Object[1] es la suma de prep1 (BigDecimal).
        List<Object[]> acumulados5MesesRaw = repository.sumLastNPrep1ForEachEstacion(5);

        // Procesar los resultados de 5 meses
        for (Object[] row : acumulados5MesesRaw) {
            Integer estacionId = (Integer) row[0];
            BigDecimal pre6 = (row[1] instanceof BigDecimal) ? (BigDecimal) row[1] : BigDecimal.ZERO;

            AcumuladoSequiaDTO dto = mapaAcumulados.get(estacionId);
            if (dto != null) {
                dto.setPre6(pre6);
            }
        }

        // 4. El resultado final es la lista de valores del mapa
        return new ArrayList<>(mapaAcumulados.values());
    }


    private Long saveIndicadorSequia(IndicadorSequiaEntity indicadorSequia) {
        IndicadorSequiaEntity savedEntity = repository.save(indicadorSequia);
        return savedEntity.getId();
    }

    @Transactional
    private BigDecimal calcularIndicadorSequia(BigDecimal valor, PesUmbralSequiaDTO umbral, Integer numeroMeses) {
        // 1- Obtener la media y desviación estándar según el número de meses
        BigDecimal media;
        BigDecimal desviacion;
        BigDecimal minPrep;
        BigDecimal maxPrep;

        switch (numeroMeses) {
            case 3:
                media = umbral.getPromedioPrep3();
                desviacion = umbral.getDesvPrep3();
                minPrep = umbral.getMinPrep3();
                maxPrep = umbral.getMaxPrep3();
                break;
            case 6:
                media = umbral.getPromedioPrep6();
                desviacion = umbral.getDesvPrep6();
                minPrep = umbral.getMinPrep6();
                maxPrep = umbral.getMaxPrep6();
                break;
            case 1:
            default:
                media = umbral.getPromedioPrep1();
                desviacion = umbral.getDesvPrep1();
                minPrep = umbral.getMinPrep1();
                maxPrep = umbral.getMaxPrep1();
                break;
        }

        //2- Validaciones previas
        if (media == null) {
            throw new CalculoIndicadorException(
                    String.format("La media es null para el umbral de la estación %s, mes %s, periodo %s meses. No se puede calcular el índice.",
                            umbral.getEstacionId(), umbral.getMes(), numeroMeses)
            );
        }
        if (desviacion == null) {
            throw new CalculoIndicadorException(
                    String.format("La desviación estándar es null para el umbral de la estación %s, mes %s, periodo %s meses. No se puede calcular el índice.",
                            umbral.getEstacionId(), umbral.getMes(), numeroMeses)
            );
        }
        if (minPrep == null || maxPrep == null) {
            throw new CalculoIndicadorException(
                    String.format("minPrep (%s) o maxPrep (%s) es null para el umbral de la estación %s, mes %s, periodo %s meses.",
                            minPrep, maxPrep, umbral.getEstacionId(), umbral.getMes(), numeroMeses)
            );
        }
        if (desviacion.compareTo(BigDecimal.ZERO) < 0) {
            throw new CalculoIndicadorException(
                    String.format("La desviación estándar es negativa (%s) para el umbral de la estación %s, mes %s, periodo %s meses. No se puede calcular el índice.",
                            desviacion, umbral.getEstacionId(), umbral.getMes(), numeroMeses)
            );
        }

        //3- Calcular los umbrales de probabilidad usando la función inversa de la normal
        BigDecimal probabilidadPreAlerta;
        BigDecimal probabilidadAlerta;
        BigDecimal probabilidadEmergencia;

        // Si la desviación estándar es cero, todos los valores históricos fueron iguales a la media.
        if (desviacion.compareTo(BigDecimal.ZERO) == 0) {
            probabilidadPreAlerta = media;
            probabilidadAlerta = media;
            probabilidadEmergencia = media;
        } else { // Caso estándar: desviación > 0
            probabilidadPreAlerta = IndicadorUtils.invNormal(FACTOR_PRE_ALERTA, media, desviacion);
            probabilidadAlerta = IndicadorUtils.invNormal(FACTOR_ALERTA, media, desviacion);
            probabilidadEmergencia = IndicadorUtils.invNormal(FACTOR_EMERGENCIA, media, desviacion);
        }

        // 4- Calcular el índice de sequía lineal multiplicativo
        return IndicadorUtils.IE_LinealMult(
                valor,
                probabilidadPreAlerta, // Umbral de prealerta
                probabilidadAlerta,    // Umbral de alerta
                probabilidadEmergencia, // Umbral de emergencia
                minPrep, // Mínimo absoluto según período
                maxPrep  // Máximo absoluto según período
        );
    }
}