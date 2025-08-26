package com.chc.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;

@Service
@RequiredArgsConstructor
public class IndicadorUtEscasezService {
    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;

    @Transactional
    public void calcularYGuardarIndicadoresUtEscasez(Integer medicionId, Integer pesId) {
        // Eliminar registros existentes para el medicionId dado
        indicadorUtEscasezRepository.deleteByMedicionId(medicionId);

        // Insertar los nuevos registros calculados
        indicadorUtEscasezRepository.insertIndicadorUtEscasez(medicionId, pesId);
    }

    public void limpiarIndicadoresUtEscasez(Integer medicionId) {
        indicadorUtEscasezRepository.deleteByMedicionId(medicionId);
    }
}

