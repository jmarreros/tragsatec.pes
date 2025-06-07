package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.estructura.PesUmbralSequiaDTO;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.dto.medicion.MedicionDTO;
import tragsatec.pes.exception.CalculoIndicadorException;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorSequiaRepository;
import tragsatec.pes.service.estructura.PesUmbralSequiaService;
import tragsatec.pes.service.medicion.DetalleMedicionService;
import tragsatec.pes.service.medicion.MedicionService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicadorSequiaService {

    private final IndicadorSequiaRepository repository;
    private final MedicionService medicionService;
    private final DetalleMedicionService detalleMedicionService;
    private final PesUmbralSequiaService pesUmbralSequiaService;

    @Transactional
    public void calcularIndicadorSequia() { // Cambiado el tipo de retorno a void
        // 1- Revisar la tabla de mediciones para obtener la medición de sequia pendiente de calcular
        MedicionDTO medicion = medicionService.findFirstNotProcessedMedicionByTipo('S');
        if (medicion == null) {
            throw new CalculoIndicadorException("No hay mediciones de sequía pendientes de calcular.");
        }

        // 2- Obtener el detalle de la medicion de la tabla de detalles de mediciones por Id
        Integer medicion_id = medicion.getId();
        Short anio = medicion.getAnio();
        Byte mes = medicion.getMes();
        Integer pes_id = medicion.getPesId();

        List<DetalleMedicionDTO> detalle = detalleMedicionService.findByMedicionId(medicion_id);
        if (detalle.isEmpty()) {
            throw new CalculoIndicadorException("No hay detalles de medición para la medición ID: " + medicion_id);
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
            calcularIndicadorSequiaEstacion(estacionId, valor, anio, mes, medicion_id);
        }

        // Marcar la medición como procesada si todo fue exitoso
        medicion.setProcesado(true);
        medicionService.update(medicion_id, medicion); // Asumiendo que update actualiza el estado procesado

    }

    @Transactional
    public void calcularIndicadorSequiaEstacion(Integer estacionId, BigDecimal valor, Short anio, Byte mes, Integer medicionId) {
        // Obtener los umbrales de sequía para la estación, año y mes específicos


        // Crear nueva entidad para almacenar resultados
        IndicadorSequiaEntity indicador = new IndicadorSequiaEntity();
        indicador.setAnio(anio);
        indicador.setMes(mes);
        indicador.setEstacionId(estacionId);

        repository.save(indicador);
    }

}