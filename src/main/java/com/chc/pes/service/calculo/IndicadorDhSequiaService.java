package com.chc.pes.service.calculo; // O el paquete donde desees ubicar tu servicio

import com.chc.pes.persistence.entity.calculo.IndicadorDhSequiaEntity;
import com.chc.pes.persistence.entity.calculo.IndicadorUtSequiaEntity;
import com.chc.pes.persistence.entity.estructura.PesDemarcacionUtEntity;
import com.chc.pes.persistence.repository.calculo.IndicadorUtSequiaRepository;
import com.chc.pes.persistence.repository.estructura.PesDemarcacionUtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.persistence.repository.calculo.IndicadorDhSequiaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicadorDhSequiaService {

    private final IndicadorDhSequiaRepository indicadorDhSequiaRepository;
    private final IndicadorUtSequiaRepository indicadorUtSequiaRepository;
    private final PesDemarcacionUtRepository pesDhUtRepository;
    private final Integer roundScale = 8;

    @Transactional
    public void calcularYGuardarIndicadoresDhSequia(Integer medicionId, Integer pesId) {
        //1- Eliminar registros existentes para el medicionId dado
        indicadorDhSequiaRepository.deleteByMedicionId(medicionId);

        //2- Recuperar los indicadores de sequía a nivel UT para la medición dada
        List<IndicadorUtSequiaEntity> indicadoresUtSequia = indicadorUtSequiaRepository.findByMedicionId(medicionId);
        if (indicadoresUtSequia.isEmpty()) {
            return; // No hay indicadores de UT para la medición dada
        }

        //3- Recuperar el coeficiente de cada UT para el PES dado
        List<PesDemarcacionUtEntity> utCoeficienteDH = pesDhUtRepository.findByPesIdAndTipo(pesId, 'S');

        //4- Hacer un Distinct de todas las demarcaciones hidrográficas involucradas
        List<Integer> demarcacionesHidrograficas = utCoeficienteDH.stream()
                .map(pdhut -> pdhut.getDemarcacion().getId())
                .distinct()
                .toList();

        //5- Recorrer cada Demarcación Hidrográfica y calcular los indicadores
        for (Integer dhId : demarcacionesHidrograficas) {
            //5.1 - Filtrar las UTs que pertenecen a la DH actual
            List<PesDemarcacionUtEntity> utsEnDH = utCoeficienteDH.stream()
                    .filter(pdhut -> pdhut.getDemarcacion().getId().equals(dhId))
                    .toList();

            //5.2 - Calcular la suma total de coeficientes para las UTs que tienen indicador no nulo
            BigDecimal sumaCoeficientesValidos = utsEnDH.stream()
                    .filter(pdhut -> {
                        IndicadorUtSequiaEntity indicador = indicadoresUtSequia.stream()
                                .filter(ind -> ind.getUnidadTerritorialId().equals(pdhut.getUnidadTerritorial().getId()))
                                .findFirst()
                                .orElse(null);
                        return indicador != null && indicador.getIeB1() != null;
                    })
                    .map(PesDemarcacionUtEntity::getCoeficiente)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            //5.3 - Calcular el porcentaje faltante para redistribuirlo entre las UTs con valor
            BigDecimal porcentajeFaltante = BigDecimal.valueOf(100).subtract(sumaCoeficientesValidos);

            //5.4 - Inicializar variables para acumular los valores ponderados
            BigDecimal prep1Ponderado = BigDecimal.ZERO;
            BigDecimal prep3Ponderado = BigDecimal.ZERO;
            BigDecimal ieB1Ponderado = BigDecimal.ZERO;
            BigDecimal ieB3Ponderado = BigDecimal.ZERO;
            int cantidadUTs = 0;

            //5.5 - Recorrer las UTs de la DH para calcular los indicadores ponderados
            for (PesDemarcacionUtEntity pdhut : utsEnDH) {
                //5.5.1 - Obtener el indicador de sequía para la UT actual
                IndicadorUtSequiaEntity indicador = indicadoresUtSequia.stream()
                        .filter(ind -> ind.getUnidadTerritorialId().equals(pdhut.getUnidadTerritorial().getId()))
                        .findFirst()
                        .orElse(null);

                //5.5.2 - Si el indicador no es nulo, acumular los valores ponderados
                if (indicador != null && indicador.getPrep1() != null && indicador.getIeB1() != null) {
                    // Obtener el coeficiente de la UT
                    BigDecimal coeficiente = pdhut.getCoeficiente();

                    // Si hay un porcentaje faltante, redistribuirlo proporcionalmente
                    if (porcentajeFaltante.compareTo(BigDecimal.ZERO) > 0) {
                        coeficiente = coeficiente.add(coeficiente.divide(sumaCoeficientesValidos, roundScale, RoundingMode.HALF_UP).multiply(porcentajeFaltante));
                    }

                    // Acumular los valores ponderados
                    cantidadUTs = cantidadUTs + 1;
                    prep1Ponderado = prep1Ponderado.add(indicador.getPrep1().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));
                    ieB1Ponderado = ieB1Ponderado.add(indicador.getIeB1().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));

                    if (indicador.getPrep3() != null)
                        prep3Ponderado = prep3Ponderado.add(indicador.getPrep3().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));

                    if (indicador.getIeB3() != null)
                        ieB3Ponderado = ieB3Ponderado.add(indicador.getIeB3().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));
                }
            }

            // Grabar los valores en la DH solo si hay al menos una UT con valor
            if (cantidadUTs > 0) {
                // Recupero el año y mes de cualquiera de los indicadores (todos son de la misma medición)
                IndicadorUtSequiaEntity indicador = indicadoresUtSequia.get(0);

                IndicadorDhSequiaEntity indicadorDhSequiaEntity = new IndicadorDhSequiaEntity();
                indicadorDhSequiaEntity.setMedicionId(medicionId);
                indicadorDhSequiaEntity.setDemarcacionId(dhId);
                indicadorDhSequiaEntity.setPrep1(prep1Ponderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorDhSequiaEntity.setPrep3(prep3Ponderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorDhSequiaEntity.setIeB1(ieB1Ponderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorDhSequiaEntity.setIeB3(ieB3Ponderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorDhSequiaEntity.setAnio(indicador.getAnio());
                indicadorDhSequiaEntity.setMes(indicador.getMes());
                indicadorDhSequiaEntity.setCantidad(cantidadUTs);
                indicadorDhSequiaRepository.save(indicadorDhSequiaEntity);
            }
        }
    }


    public void limpiarIndicadoresDhSequia(Integer medicionId) {
        indicadorDhSequiaRepository.deleteByMedicionId(medicionId);
    }
}