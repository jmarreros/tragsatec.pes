package com.chc.pes.service.calculo;

import com.chc.pes.persistence.entity.calculo.IndicadorEscasezEntity;
import com.chc.pes.persistence.entity.calculo.IndicadorUtEscasezEntity;
import com.chc.pes.persistence.entity.estructura.PesUtEstacionEntity;
import com.chc.pes.persistence.repository.calculo.IndicadorEscasezRepository;
import com.chc.pes.persistence.repository.estructura.PesUtEstacionRepository;
import com.chc.pes.util.Escenario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndicadorUtEscasezService {
    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;
    private final IndicadorEscasezRepository indicadorEscasezRepository;
    private final PesUtEstacionRepository pesUtEstacionRepository;
    private final Integer roundScale = 8;

    @Transactional
    public void calcularYGuardarIndicadoresUtEscasez(Integer medicionId, Integer pesId) {
        // 1- Eliminar registros existentes para el medicionId dado
        indicadorUtEscasezRepository.deleteByMedicionId(medicionId);

        // 2- Recuperar los indicadores de escasez ya calculados por estación
        List<IndicadorEscasezEntity> indicadoresEscasez = indicadorEscasezRepository.findByMedicionId(medicionId);
        if (indicadoresEscasez.isEmpty()) {
            return; // No hay indicadores de escasez para la medición dada
        }

        // 3- Recuperar el coeficiente de cada estación para el PES dado
        List<PesUtEstacionEntity> estacionesCoeficienteUT = pesUtEstacionRepository.findByPesIdAndTipo(pesId, 'E');

        // 4- Hacer un Distinct de todas las unidades territoriales involucradas
        List<Integer> unidadesTerritoriales = estacionesCoeficienteUT.stream()
                .map(pute -> pute.getUnidadTerritorial().getId())
                .distinct()
                .toList();

        // 5- Recorrer cada Unidad Territorial y calcular los indicadores
        for (Integer utId : unidadesTerritoriales) {
            // 5.1 - Filtrar estaciones de la unidad territorial actual
            List<PesUtEstacionEntity> estacionesEnUT = estacionesCoeficienteUT.stream()
                    .filter(pute -> pute.getUnidadTerritorial().getId().equals(utId))
                    .toList();

            // 5.2 - Calcular suma de coeficientes para estaciones con indicador no nulo
            BigDecimal sumaCoeficientesValidos = estacionesEnUT.stream()
                    .filter(pute -> {
                        IndicadorEscasezEntity indicador = indicadoresEscasez.stream()
                                .filter(ind -> ind.getEstacionId().equals(pute.getEstacion().getId()))
                                .findFirst()
                                .orElse(null);
                        return indicador != null && indicador.getIe() != null;
                    })
                    .map(PesUtEstacionEntity::getCoeficiente)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 5.3 - Calcular porcentaje faltante para redistribuir
            BigDecimal porcentajeFaltante = BigDecimal.valueOf(100).subtract(sumaCoeficientesValidos);

            // 5.4 - Inicializar variables para acumular valores
            BigDecimal datoPonderado = BigDecimal.ZERO;
            BigDecimal indicePonderado = BigDecimal.ZERO;
            int cantidadEstaciones = 0;

            // 5.5 - Recorrer estaciones y calcular valores ponderados
            for (PesUtEstacionEntity pute : estacionesEnUT) {
                IndicadorEscasezEntity indicador = indicadoresEscasez.stream()
                        .filter(ind -> ind.getEstacionId().equals(pute.getEstacion().getId()))
                        .findFirst()
                        .orElse(null);

                if (indicador != null && indicador.getIe() != null) {
                    BigDecimal coeficiente = pute.getCoeficiente();

                    // Redistribuir el porcentaje faltante proporcionalmente
                    if (porcentajeFaltante.compareTo(BigDecimal.ZERO) > 0 && sumaCoeficientesValidos.compareTo(BigDecimal.ZERO) > 0) {
                        coeficiente = coeficiente.add(coeficiente
                                .divide(sumaCoeficientesValidos, roundScale, RoundingMode.HALF_UP)
                                .multiply(porcentajeFaltante));
                    }

                    // Acumular valores ponderados
                    cantidadEstaciones++;

                    datoPonderado = datoPonderado.add(indicador.getDato()
                            .multiply(coeficiente)
                            .divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));

                    indicePonderado = indicePonderado.add(indicador.getIe()
                            .multiply(coeficiente)
                            .divide(BigDecimal.valueOf(100), roundScale, RoundingMode.HALF_UP));
                }
            }

            // 5.6 - Guardar resultados si hay al menos una estación con valor
            if (cantidadEstaciones > 0) {
                IndicadorEscasezEntity indicador = indicadoresEscasez.get(0);

                IndicadorUtEscasezEntity indicadorUt = new IndicadorUtEscasezEntity();
                indicadorUt.setMedicionId(medicionId);
                indicadorUt.setUnidadTerritorialId(utId);
                indicadorUt.setDato(datoPonderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorUt.setIe(indicePonderado.setScale(roundScale, RoundingMode.HALF_UP));
                indicadorUt.setAnio(indicador.getAnio());
                indicadorUt.setMes(indicador.getMes());
                indicadorUt.setCantidad(cantidadEstaciones);

                // Calcular escenarios inicial y final
                calcularEscenarios(indicadorUt);

                indicadorUtEscasezRepository.save(indicadorUt);
            }
        }
    }

    public void limpiarIndicadoresUtEscasez(Integer medicionId) {
        indicadorUtEscasezRepository.deleteByMedicionId(medicionId);
    }

    private void calcularEscenarios(IndicadorUtEscasezEntity indicadorUt) {
        YearMonth currentMonth = YearMonth.of(indicadorUt.getAnio(), indicadorUt.getMes());
        YearMonth previousMonth = currentMonth.minusMonths(1);

        Optional<IndicadorUtEscasezEntity> indicadorAnteriorOpt = indicadorUtEscasezRepository
                .findByUnidadTerritorialIdAndAnioAndMes(indicadorUt.getUnidadTerritorialId(), previousMonth.getYear(), previousMonth.getMonthValue());

        String escenarioInicialStr = indicadorAnteriorOpt
                .map(IndicadorUtEscasezEntity::getEscenarioFinal)
                .orElse(Escenario.NORMALIDAD.getValue());

        indicadorUt.setEscenarioInicial(escenarioInicialStr);

        Escenario escenarioInicial = Escenario.fromValue(escenarioInicialStr);
        Escenario escenarioFinal = escenarioInicial; // Por defecto, se mantiene el escenario

        BigDecimal ieActual = indicadorUt.getIe();
        BigDecimal ieAnterior = indicadorAnteriorOpt.map(IndicadorUtEscasezEntity::getIe).orElse(null);

        // --- LÓGICA DE SALIDA (MEJORA DE ESCENARIO) ---
        if (ieActual.compareTo(new BigDecimal("0.5")) >= 0) {
            escenarioFinal = Escenario.NORMALIDAD;
        } else if (escenarioInicial == Escenario.EMERGENCIA && ieActual.compareTo(new BigDecimal("0.3")) >= 0) {
            escenarioFinal = Escenario.PREALERTA;
        } else if ((escenarioInicial == Escenario.EMERGENCIA || escenarioInicial == Escenario.ALERTA) &&
                ieActual.compareTo(new BigDecimal("0.3")) >= 0 && ieActual.compareTo(new BigDecimal("0.5")) < 0) {
            escenarioFinal = Escenario.PREALERTA;
        } else if (escenarioInicial == Escenario.EMERGENCIA &&
                ieActual.compareTo(new BigDecimal("0.15")) >= 0 && ieActual.compareTo(new BigDecimal("0.3")) < 0) {
            escenarioFinal = Escenario.ALERTA;
        }

        // --- LÓGICA DE ENTRADA (EMPEORAMIENTO DE ESCENARIO) ---
        if (escenarioFinal == escenarioInicial) { // Solo si no ha habido una mejora
            boolean dosMesesConsecutivos = ieAnterior != null;

            switch (escenarioInicial) {
                case NORMALIDAD:
                    if (ieActual.compareTo(new BigDecimal("0.15")) < 0) {
                        escenarioFinal = Escenario.ALERTA;
                    } else if (ieActual.compareTo(new BigDecimal("0.3")) < 0) {
                        escenarioFinal = Escenario.PREALERTA;
                    } else if (dosMesesConsecutivos &&
                            ieActual.compareTo(new BigDecimal("0.3")) >= 0 && ieActual.compareTo(new BigDecimal("0.5")) < 0 &&
                            ieAnterior.compareTo(new BigDecimal("0.3")) >= 0 && ieAnterior.compareTo(new BigDecimal("0.5")) < 0) {
                        escenarioFinal = Escenario.PREALERTA;
                    }
                    break;
                case PREALERTA:
                    if (ieActual.compareTo(new BigDecimal("0.15")) < 0) {
                        escenarioFinal = Escenario.ALERTA;
                    } else if (dosMesesConsecutivos &&
                            ieActual.compareTo(new BigDecimal("0.15")) >= 0 && ieActual.compareTo(new BigDecimal("0.3")) < 0 &&
                            ieAnterior.compareTo(new BigDecimal("0.15")) >= 0 && ieAnterior.compareTo(new BigDecimal("0.3")) < 0) {
                        escenarioFinal = Escenario.ALERTA;
                    } else if (dosMesesConsecutivos &&
                            ieActual.compareTo(new BigDecimal("0.15")) < 0 && ieAnterior.compareTo(new BigDecimal("0.15")) < 0) {
                        escenarioFinal = Escenario.EMERGENCIA;
                    }
                    break;
                case ALERTA:
                    if (dosMesesConsecutivos &&
                            ieActual.compareTo(new BigDecimal("0.15")) < 0 && ieAnterior.compareTo(new BigDecimal("0.15")) < 0) {
                        escenarioFinal = Escenario.EMERGENCIA;
                    }
                    break;
                case EMERGENCIA:
                    // No hay transiciones de entrada desde Emergencia
                    break;
            }
        }

        indicadorUt.setEscenarioFinal(escenarioFinal.getValue());
    }
}