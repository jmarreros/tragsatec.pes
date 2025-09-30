package com.chc.pes.service.calculo;

import com.chc.pes.persistence.entity.calculo.IndicadorDhEscasezEntity;
import com.chc.pes.persistence.entity.calculo.IndicadorUtEscasezEntity;
import com.chc.pes.persistence.entity.estructura.PesDemarcacionUtEntity;
import com.chc.pes.persistence.repository.calculo.IndicadorDhEscasezRepository;
import com.chc.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;
import com.chc.pes.persistence.repository.estructura.PesDemarcacionUtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicadorDhEscasezService {
    private final IndicadorDhEscasezRepository indicadorDhEscasezRepository;
    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;
    private final PesDemarcacionUtRepository pesDhUtRepository;
    private final Integer roundScale = 8;

    @Transactional
    public void calcularYGuardarIndicadoresDhEscasez(Integer medicionId, Integer pesId) {
        // Eliminar registros existentes para el medicionId dado
        indicadorDhEscasezRepository.deleteByMedicionId(medicionId);

        //2- Recuperar los indicadores de escasez a nivel UT para la medición dada
        List<IndicadorUtEscasezEntity> indicadoresUtEscasez = indicadorUtEscasezRepository.findByMedicionId(medicionId);
        if (indicadoresUtEscasez.isEmpty()) {
            return; // No hay indicadores de UT para la medición dada
        }

        //3- Recuperar el coeficiente de cada UT para el PES dado
        List<PesDemarcacionUtEntity> utCoeficienteDH = pesDhUtRepository.findByPesIdAndTipo(pesId, 'E');

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
                        IndicadorUtEscasezEntity indicador = indicadoresUtEscasez.stream()
                                .filter(ind -> ind.getUnidadTerritorialId().equals(pdhut.getUnidadTerritorial().getId()))
                                .findFirst()
                                .orElse(null);
                        return indicador != null && indicador.getIe() != null;
                    })
                    .map(PesDemarcacionUtEntity::getCoeficiente)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            //5.3 - Calcular el porcentaje faltante para redistribuirlo entre las UTs con valor
            BigDecimal porcentajeFaltante = BigDecimal.valueOf(100).subtract(sumaCoeficientesValidos);

            //5.4 - Inicializar variables para acumular los valores ponderados
            BigDecimal iePonderado = BigDecimal.ZERO;
            BigDecimal datoPonderado = BigDecimal.ZERO;
            int cantidadUTs = 0;

            //5.5 - Recorrer las UTs de la DH para calcular los indicadores ponderados
            for (PesDemarcacionUtEntity pdhut : utsEnDH) {
                //5.5.1 - Obtener el indicador de escasez para la UT actual
                IndicadorUtEscasezEntity indicador = indicadoresUtEscasez.stream()
                        .filter(ind -> ind.getUnidadTerritorialId().equals(pdhut.getUnidadTerritorial().getId()))
                        .findFirst()
                        .orElse(null);

                //5.5.2 - Si el indicador no es nulo, acumular los valores ponderados
                if (indicador != null && indicador.getIe() != null) {
                    // Obtener el coeficiente de la UT
                    BigDecimal coeficiente = pdhut.getCoeficiente();

                    // Si hay un porcentaje faltante, redistribuirlo proporcionalmente
                    if (porcentajeFaltante.compareTo(BigDecimal.ZERO) > 0) {
                        coeficiente = coeficiente.add(coeficiente.divide(sumaCoeficientesValidos, roundScale, RoundingMode.HALF_UP).multiply(porcentajeFaltante));
                    }

                    // Acumular los valores ponderados
                    cantidadUTs = cantidadUTs + 1;
                    iePonderado = iePonderado.add(indicador.getIe().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));
                    datoPonderado = datoPonderado.add(indicador.getDato().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));
                }
            }

            // Grabar los valores en la DH solo si hay al menos una UT con valor
            if (cantidadUTs > 0) {
                // Recupero el año y mes de cualquiera de los indicadores (todos son de la misma medición)
                IndicadorUtEscasezEntity indicador = indicadoresUtEscasez.get(0);

                IndicadorDhEscasezEntity indicadorDhEscasezEntity = new IndicadorDhEscasezEntity();
                indicadorDhEscasezEntity.setMedicionId(medicionId);
                indicadorDhEscasezEntity.setDemarcacionId(dhId);
                indicadorDhEscasezEntity.setIe(iePonderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorDhEscasezEntity.setDato(datoPonderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorDhEscasezEntity.setAnio(indicador.getAnio());
                indicadorDhEscasezEntity.setMes(indicador.getMes());
                indicadorDhEscasezEntity.setCantidad(cantidadUTs);
                indicadorDhEscasezRepository.save(indicadorDhEscasezEntity);
            }
        }
    }

    public void limpiarIndicadoresDhEscasez(Integer medicionId) {
        indicadorDhEscasezRepository.deleteByMedicionId(medicionId);
    }
}