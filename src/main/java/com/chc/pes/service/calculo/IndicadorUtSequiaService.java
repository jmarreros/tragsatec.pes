package com.chc.pes.service.calculo; // O el paquete donde esté tu servicio

import com.chc.pes.persistence.entity.calculo.IndicadorSequiaEntity;
import com.chc.pes.persistence.entity.calculo.IndicadorUtSequiaEntity;
import com.chc.pes.persistence.entity.estructura.PesUtEstacionEntity;
import com.chc.pes.persistence.repository.calculo.IndicadorSequiaRepository;
import com.chc.pes.persistence.repository.estructura.PesUtEstacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.persistence.repository.calculo.IndicadorUtSequiaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicadorUtSequiaService {

    private final IndicadorUtSequiaRepository indicadorUtSequiaRepository;
    private final IndicadorSequiaRepository indicadorSequiaRepository;
    private final PesUtEstacionRepository pesUtEstacionRepository;
    private final Integer roundScale = 8;

    @Transactional
    public void calcularYGuardarIndicadoresUtSequia(Integer medicionId, Integer pesId) {
        //1- Eliminar registros existentes para el medicionId dado
        indicadorUtSequiaRepository.deleteByMedicionId(medicionId);

        //2- Recuperar los indicadores de sequía ya calculados por estación para la medición dada
        List<IndicadorSequiaEntity> indicadoresSequia = indicadorSequiaRepository.findByMedicionId(medicionId);
        if (indicadoresSequia.isEmpty()) {
            return; // No hay indicadores de sequía para la medición dada, salir del método
        }

        //3- Recuperar el coeficiente de cada estación para el PES dado
        List<PesUtEstacionEntity> estacionesCoeficienteUT = pesUtEstacionRepository.findByPesIdAndTipo(pesId, 'S');

        //4- Hacer un Distinct de todas las unidades territoriales involucradas
        List<Integer> unidadesTerritoriales = estacionesCoeficienteUT.stream()
                .map(pute -> pute.getUnidadTerritorial().getId())
                .distinct()
                .toList();

        //5- Recorrer cada Unidad Territorial y calcular los indicadores de acuerdo al % de coeficiente y el valor de cada estación en indicadoresSequia
        for (Integer utId : unidadesTerritoriales) {

            //5.1 - Filtrar las estaciones que pertenecen a la unidad territorial actual
            List<PesUtEstacionEntity> estacionesEnUT = estacionesCoeficienteUT.stream()
                    .filter(pute -> pute.getUnidadTerritorial().getId().equals(utId))
                    .toList();

            //5.2 - Calcular la suma total de coeficientes para las estaciones que tienen indicador no nulo
            BigDecimal sumaCoeficientesValidos = estacionesEnUT.stream()
                    .filter(pute -> {
                        IndicadorSequiaEntity indicador = indicadoresSequia.stream()
                                .filter(ind -> ind.getEstacionId().equals(pute.getEstacion().getId()))
                                .findFirst()
                                .orElse(null);
                        return indicador != null && indicador.getIeB1() != null;
                    })
                    .map(PesUtEstacionEntity::getCoeficiente)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            //5.3 - Calcular el porcentaje faltante para redistribuirlo entre las estaciones con valor
            BigDecimal porcentajeFaltante = BigDecimal.valueOf(100).subtract(sumaCoeficientesValidos);

            //5.4 - Inicializar variables para acumular los valores ponderados
            BigDecimal prep1Ponderado = BigDecimal.ZERO;
            BigDecimal prep3Ponderado = BigDecimal.ZERO;
            BigDecimal ieB1Ponderado = BigDecimal.ZERO;
            BigDecimal ieB3Ponderado = BigDecimal.ZERO;
            int cantidadEstaciones = 0;

            //5.5 - Recorrer las estaciones de la unidad territorial para calcular los indicadores ponderados
            for (PesUtEstacionEntity pute : estacionesEnUT) {

                //5.5.1 - Obtener el indicador de sequía para la estación actual
                IndicadorSequiaEntity indicador = indicadoresSequia.stream()
                        .filter(ind -> ind.getEstacionId().equals(pute.getEstacion().getId()))
                        .findFirst()
                        .orElse(null);

                //5.5.2 - Si el indicador no es nulo, acumular los valores ponderados
                if (indicador != null && indicador.getPrep1() != null && indicador.getIeB1() != null) {
                    // Obtener el coeficiente de la estación
                    BigDecimal coeficiente = pute.getCoeficiente();

                    // Si hay un porcentaje faltante, redistribuirlo proporcionalmente
                    if ( porcentajeFaltante.compareTo(BigDecimal.ZERO) > 0) {
                        coeficiente = coeficiente.add(coeficiente.divide(sumaCoeficientesValidos, roundScale, RoundingMode.HALF_UP).multiply(porcentajeFaltante));
                    }

                    // Acumular los valores ponderados
                    cantidadEstaciones = cantidadEstaciones + 1;
                    prep1Ponderado = prep1Ponderado.add(indicador.getPrep1().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));
                    ieB1Ponderado = ieB1Ponderado.add(indicador.getIeB1().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));

                    if (indicador.getPrep3() != null)
                        prep3Ponderado = prep3Ponderado.add(indicador.getPrep3().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));

                    if (indicador.getIeB3() != null)
                        ieB3Ponderado = ieB3Ponderado.add(indicador.getIeB3().multiply(coeficiente).divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));
                }
            }

            // Grabar los valores en la Unidad Territorial solo si hay al menos una estación con valor
            if (cantidadEstaciones > 0) {
                // Recupero el año y mes de cualquiera de los indicadores (todos son de la misma medición)
                IndicadorSequiaEntity indicador = indicadoresSequia.get(0);

                IndicadorUtSequiaEntity indicadorUtSequiaEntity = new IndicadorUtSequiaEntity();
                indicadorUtSequiaEntity.setMedicionId(medicionId);
                indicadorUtSequiaEntity.setUnidadTerritorialId(utId);
                indicadorUtSequiaEntity.setPrep1(prep1Ponderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorUtSequiaEntity.setPrep3(prep3Ponderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorUtSequiaEntity.setIeB1(ieB1Ponderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorUtSequiaEntity.setIeB3(ieB3Ponderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorUtSequiaEntity.setAnio(indicador.getAnio());
                indicadorUtSequiaEntity.setMes(indicador.getMes());
                indicadorUtSequiaEntity.setCantidad(cantidadEstaciones);

                // Calcular el escenario final basado en ieB3
                calcularEscenario(indicadorUtSequiaEntity);

                indicadorUtSequiaRepository.save(indicadorUtSequiaEntity);
            }

        }
    }

    public void limpiarIndicadoresUtSequia(Integer medicionId) {
        indicadorUtSequiaRepository.deleteByMedicionId(medicionId);
    }


    // Si ieB3 < 0.3 => Escenario Final: "Sequia prolongada"
    // Si ieB3 >= 0.3  => Escenario Final: "Condiciones normales"
    public void calcularEscenario(IndicadorUtSequiaEntity indicadorUtSequiaEntity) {
        BigDecimal umbral = new BigDecimal("0.3");
        String escenarioFinal = "";
        if (indicadorUtSequiaEntity.getIeB3() != null) {
            if (indicadorUtSequiaEntity.getIeB3().compareTo(umbral) < 0) {
                escenarioFinal = "Sequia prolongada";
            } else {
                escenarioFinal = "Condiciones normales";
            }
        }
        indicadorUtSequiaEntity.setEscenarioFinal(escenarioFinal);
    }
}