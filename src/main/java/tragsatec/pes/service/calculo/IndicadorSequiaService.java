package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.estructura.PesUmbralSequiaDTO;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.dto.medicion.MedicionDTO;
import tragsatec.pes.exception.CalculoIndicadorException;
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
    public Boolean calcularIndicadorSequia() {
        // 1- Revisar la tabla de mediciones para obtener la medición de sequia pendiente de calcular
        MedicionDTO medicion = medicionService.findFirstNotProcessedMedicionByTipo('S');
        if (medicion == null) {
            throw new CalculoIndicadorException("No hay mediciones de sequía pendientes de calcular.");
        }

        // 2- Obtener el detalle de la medicion de la tabla de detalles de mediciones por Id
        Integer medicionId = medicion.getId();
        Short anio = medicion.getAnio();
        Byte mes = medicion.getMes();
        Integer pes_id = medicion.getPesId();

        List<DetalleMedicionDTO> detalle = detalleMedicionService.findByMedicionId(medicionId);
        if (detalle.isEmpty()) {
            throw new CalculoIndicadorException("No hay detalles de medición para la medición ID: " + medicionId);
        }

        //3- Obtener todos los umbrales para el PES y mes actual de una vez
        List<PesUmbralSequiaDTO> umbralesSequia = pesUmbralSequiaService.findByPesIdAndMes(pes_id, mes);
        if (umbralesSequia.isEmpty()) {
            throw new CalculoIndicadorException("No se encontraron umbrales de sequía para PES ID: " + pes_id + " y mes: " + mes);
        }

        // 3- Recorrer el detalle de la medición y calcular el indicador de sequía para cada estación
        for (DetalleMedicionDTO detalleMedicion : detalle) {
            // 3.1- Obtener el ID de la estación desde el detalle de medición
            Integer estacionId = detalleMedicion.getEstacionId();
            BigDecimal valor = detalleMedicion.getValor();

            // 3.2- Buscar el umbral para la estación actual en la lista cargada
            PesUmbralSequiaDTO umbralEstacionActual = umbralesSequia.stream()
                    .filter(u -> u.getEstacionId().equals(estacionId))
                    .findFirst()
                    .orElseThrow(() -> new CalculoIndicadorException("No se encontraron umbrales de sequía para PES ID: " + pes_id + ", estación: " + estacionId + ", mes: " + mes));

            // 3.3- Calcular el indicador de sequía para la estación, año y mes específicos
            BigDecimal indiceEstacion = calcularIndice(valor, umbralEstacionActual);

            // 3.4- Guardar el resultado del indicador de sequía en la base de datos
            saveIndicadorSequia(medicionId, estacionId, anio, mes, valor, indiceEstacion);

        }

        // Marcar la medición como procesada si todo fue exitoso
//        medicion.setProcesado(true);
//        medicionService.update(medicionId, medicion); // Asumiendo que update actualiza el estado procesado

        return true;
    }

    private void saveIndicadorSequia(Integer medicionId, Integer estacionId, Short anio, Byte mes, BigDecimal valor, BigDecimal indiceEstacion) {
        System.out.println("Guardando indicador de sequía: " +
                "Medición ID: " + medicionId +
                ", Estación ID: " + estacionId +
                ", Año: " + anio +
                ", Mes: " + mes +
                ", Valor: " + valor +
                ", Índice: " + indiceEstacion);
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