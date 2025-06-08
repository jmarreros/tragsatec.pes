package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.AcumuladoSequia;
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
import java.util.List;

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
        // 1- Revisar la tabla de mediciones para obtener la medición de sequia pendiente de calcular
        MedicionDTO medicion = medicionService.findFirstNotProcessedMedicionByTipo('S');
        if (medicion == null) {
            throw new CalculoIndicadorException("No hay mediciones de sequía pendientes de calcular.");
        }

        // 2- Obtener el detalle de la medicion de la tabla de detalles de mediciones por Id
        Integer medicionId = medicion.getId();
        Byte mes = medicion.getMes();
        Integer pes_id = medicion.getPesId();

        List<DetalleMedicionDTO> detallesMedicion = detalleMedicionService.findByMedicionId(medicionId);
        if (detallesMedicion.isEmpty()) {
            throw new CalculoIndicadorException("No hay detalles de medición para la medición ID: " + medicionId);
        }

        //3- Borrar los indicadores de sequía si existen para la medición actual y volver a calcularlos
        repository.deleteByMedicionId(medicionId);

        //4- Obtener todos los umbrales para el PES y mes actual de una vez
        List<PesUmbralSequiaDTO> umbralesSequia = pesUmbralSequiaService.findByPesIdAndMes(pes_id, mes);
        if (umbralesSequia.isEmpty()) {
            throw new CalculoIndicadorException("No se encontraron umbrales de sequía para PES ID: " + pes_id + " y mes: " + mes);
        }

        //5- Para los acumulados, obtener los registros del mes anterior
        List<AcumuladoSequia> acumuladosMesAnterior = getAcumuladosMesAnterior(detallesMedicion);

        // 6- Recorrer el detalle de la medición y calcular el indicador de sequía para cada estación
        for (DetalleMedicionDTO itemDetalleMedicion : detallesMedicion) {
            // 6.1- Obtener el ID y valor de la estación desde el detalle de medición
            Integer estacionId = itemDetalleMedicion.getEstacionId();
            BigDecimal valor = itemDetalleMedicion.getValor();

            // 6.2- Buscar el umbral para la estación actual en la lista cargada
            PesUmbralSequiaDTO umbralEstacionActual = umbralesSequia.stream()
                    .filter(u -> u.getEstacionId().equals(estacionId))
                    .findFirst()
                    .orElseThrow(() -> new CalculoIndicadorException("No se encontraron umbrales de sequía para PES ID: " + pes_id + ", estación: " + estacionId + ", mes: " + mes));

            // 6.3- Calcular el indicador de sequía para la estación, año y mes específicos
            BigDecimal valorIndice = calcularIndice(valor, umbralEstacionActual);

            // 6.4- Guardar el resultado del indicador de sequía en la base de datos
            Long indicadorId = saveIndicadorSequia(medicion, estacionId, valor, valorIndice);
            if (indicadorId == null) {
                throw new CalculoIndicadorException("Error al guardar el indicador de sequía para la estación ID: " + estacionId);
            }

            // 6.5- Acumulados de valores de precipitación y el índice de sequía

        }

        // 7- Marcar la medición como procesada si fue correcto
        medicionService.marcarComoProcesada(medicionId);
    }

    private List<AcumuladoSequia> getAcumuladosMesAnterior(List<DetalleMedicionDTO> detallesMedicion) {
        //1- Obtener la última medición procesada de tipo "S"
        MedicionDTO medicion = medicionService.findLastProcessedMedicionByTipo('S');
        if (medicion == null) {
            // Construir el objeto de acumulados con valores nulos si no hay mediciones procesadas
            return detallesMedicion.stream()
                    .map(detalle -> new AcumuladoSequia(detalle.getEstacionId(), BigDecimal.ZERO, BigDecimal.ZERO))
                    .toList();
        }

        //2- Obtener los acumulados de sequía dado el ID de la medición
        Integer medicionId = medicion.getId();
        return repository.findByMedicionId(medicionId)
                .stream()
                .map(indicador -> new AcumuladoSequia(
                        indicador.getEstacionId(),
                        indicador.getPrep1(),
                        indicador.getPrep6()))
                .toList();
    }

    private Long saveIndicadorSequia(MedicionDTO medicion, Integer estacionId, BigDecimal valor, BigDecimal valorIndice) {
        IndicadorSequiaEntity indicadorSequia = new IndicadorSequiaEntity();

        // Establecemos los valores iniciales
        indicadorSequia.setMedicionId(medicion.getId());
        indicadorSequia.setMes(medicion.getMes());
        indicadorSequia.setAnio(medicion.getAnio());
        indicadorSequia.setEstacionId(estacionId);
        indicadorSequia.setPrep1(valor);
        indicadorSequia.setIeB1(valorIndice);

        IndicadorSequiaEntity savedEntity = repository.save(indicadorSequia);

        return savedEntity.getId();
    }

    @Transactional
    private BigDecimal calcularIndice(BigDecimal valor, PesUmbralSequiaDTO umbral) {
        // 1- Obtener la media y desviación estándar de los valores de precipitación
        BigDecimal media = umbral.getPromedioPrep1();
        BigDecimal desviacion = umbral.getDesvPrep1();

        // 2- Calcular la probabilidad acumulada usando la función inversa de la normal
        BigDecimal probabilidadPreAlerta = IndicadorUtils.invNormal(FACTOR_PRE_ALERTA, media, desviacion);
        BigDecimal probabilidadAlerta = IndicadorUtils.invNormal(FACTOR_ALERTA, media, desviacion);
        BigDecimal probabilidadEmergencia = IndicadorUtils.invNormal(FACTOR_EMERGENCIA, media, desviacion);

        // 3- Calcular el índice de sequía lineal multiplicativo
        return IndicadorUtils.IE_LinealMult(
                valor,
                probabilidadPreAlerta, // Umbral de prealerta
                probabilidadAlerta,    // Umbral de alerta
                probabilidadEmergencia, // Umbral de emergencia
                umbral.getMinPrep1(), // Mínimo absoluto
                umbral.getMaxPrep1()   // Máximo absoluto
        );
    }

}