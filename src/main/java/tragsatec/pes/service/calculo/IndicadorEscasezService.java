package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.dto.medicion.MedicionDTO;
import tragsatec.pes.exception.CalculoIndicadorException;
import tragsatec.pes.persistence.entity.calculo.IndicadorEscasezEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorEscasezRepository;
import tragsatec.pes.service.estructura.PesUmbralEscasezService;
import tragsatec.pes.service.medicion.DetalleMedicionService;
import tragsatec.pes.service.medicion.MedicionService;
import tragsatec.pes.util.IndicadorUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndicadorEscasezService {

    private final IndicadorEscasezRepository repository;
    private final MedicionService medicionService;
    private final DetalleMedicionService detalleMedicionService;
    private final PesUmbralEscasezService pesUmbralEscasezService;

    // Estos estadísticos se usan como claves para extraer valores del mapa de umbrales.
    // Deben coincidir con los valores generados por `escenario + estadistico` en la consulta.
    private static final String FACTOR_ESTRES_MINIMO = "EstrésMínimo";
    private static final String FACTOR_NORMALIDAD_MINIMO = "NormalidadMínimo";
    private static final String FACTOR_NORMALIDAD_MAXIMO = "NormalidadMáximo";


    @Transactional
    public void calcularIndicadorEscasez() {
        // 1- Revisar la tabla de mediciones para obtener la medición de escasez pendiente de calcular (Tipo 'E')
        MedicionDTO medicion = medicionService.findFirstNotProcessedMedicionByTipo('E');
        if (medicion == null) {
            throw new CalculoIndicadorException("No hay mediciones de escasez pendientes de calcular.");
        }

        Integer medicionId = medicion.getId();
        Byte mes = medicion.getMes();
        Integer pesId = medicion.getPesId();

        // 2- Obtener el detalle de la medicion por medicionId
        List<DetalleMedicionDTO> detallesMedicion = detalleMedicionService.findByMedicionId(medicionId);
        if (detallesMedicion.isEmpty()) {
            throw new CalculoIndicadorException("No hay detalles de medición para la medición ID: " + medicionId);
        }

        // 3- Borrar los indicadores de escasez si existen para la medición actual para volver a calcularlos
        repository.deleteByMedicionId(medicionId);

        // 4- Obtener todos los umbrales de escasez para el PES actual.
        List<Map<String, Object>> umbralesEscasez = pesUmbralEscasezService.getPivotedUmbralesPorEstacion(pesId, mes);
        if (umbralesEscasez.isEmpty()) {
            throw new CalculoIndicadorException("No se encontraron umbrales de escasez para el PES ID: " + pesId + " y mes: " + mes);
        }

        // 5- Recorrer el detalle de la medición y calcular los indicadores
        for (DetalleMedicionDTO itemDetalle : detallesMedicion) {
            Integer estacionId = itemDetalle.getEstacionId();
            BigDecimal valorMedicion = itemDetalle.getValor();

            // 5.1- Obtenemos los umbrales de escasez para la estación actual desde umbralesEscasezPivotados
            Map<String, Object> umbralesParaEstacion = umbralesEscasez.stream()
                    .filter(umbralMap -> estacionId.equals(umbralMap.get("estacion_id")))
                    .findFirst()
                    .orElse(null);

            if (umbralesParaEstacion == null) {
                throw new CalculoIndicadorException("No se encontraron umbrales de escasez para la estación ID: " + estacionId + " (PES ID: " + pesId + ", mes: " + mes + ")");
            }

            // 5.2- Calcular el indicador de escasez
            BigDecimal indicadorEscasez = calcularIndicadorEscasez(valorMedicion, umbralesParaEstacion);
            if (indicadorEscasez == null) {
                throw new CalculoIndicadorException("Error al calcular el indicador de escasez para la estación ID: " + estacionId);
            }

            // 5.3- Crear la entidad de indicador de escasez
            IndicadorEscasezEntity indicadorEscasezEntity = new IndicadorEscasezEntity();
            indicadorEscasezEntity.setEstacionId(estacionId);
            indicadorEscasezEntity.setMedicionId(medicionId);
            indicadorEscasezEntity.setDato(valorMedicion);
            indicadorEscasezEntity.setAnio(medicion.getAnio());
            indicadorEscasezEntity.setMes(mes);
            indicadorEscasezEntity.setIe(indicadorEscasez);

            // 5.4- Guardar el indicador de escasez
            Long indicadorEscasezId = saveIndicadorEscasez(indicadorEscasezEntity);
            if (indicadorEscasezId == null) {
                throw new CalculoIndicadorException("Error al guardar el indicador de escasez para la estación ID: " + estacionId);
            }
        }

        // 6- Actualizar la medición a procesada
        medicionService.marcarComoProcesada(medicionId);

    }

    private Long saveIndicadorEscasez(IndicadorEscasezEntity indicadorEscasez) {
        IndicadorEscasezEntity savedEntity = repository.save(indicadorEscasez);
        return savedEntity.getId();
    }


    private BigDecimal calcularIndicadorEscasez(BigDecimal valorMedicion, Map<String, Object> umbralesParaEstacion) {
        BigDecimal valEstresMinimo = (BigDecimal) umbralesParaEstacion.get(FACTOR_ESTRES_MINIMO);
        BigDecimal valNormalidadMinimo = (BigDecimal) umbralesParaEstacion.get(FACTOR_NORMALIDAD_MINIMO);
        BigDecimal valNormalidadMaximo = (BigDecimal) umbralesParaEstacion.get(FACTOR_NORMALIDAD_MAXIMO);

        return IndicadorUtils.IE_LinealC(valorMedicion, valNormalidadMinimo, valEstresMinimo, valNormalidadMaximo);
    }

}