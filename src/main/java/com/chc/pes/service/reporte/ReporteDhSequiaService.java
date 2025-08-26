package com.chc.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.calculo.IndicadorFechaDataProjection;
import com.chc.pes.persistence.repository.calculo.IndicadorDhSequiaRepository;

import java.util.List;

@Service
public class ReporteDhSequiaService {

    private final IndicadorDhSequiaRepository indicadorDhSequiaRepository;

    @Autowired
    public ReporteDhSequiaService(IndicadorDhSequiaRepository indicadorDhSequiaRepository) {
        this.indicadorDhSequiaRepository = indicadorDhSequiaRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer demarcacionId, String tipoPrep) {
        if ("prep1".equalsIgnoreCase(tipoPrep)) {
            return indicadorDhSequiaRepository.getAllDataIndicadorAnioMesPrep1(demarcacionId);
        } else if ("prep3".equalsIgnoreCase(tipoPrep)) {
            return indicadorDhSequiaRepository.getAllDataIndicadorAnioMesPrep3(demarcacionId);
        } else {
            throw new IllegalArgumentException("El tipo de precipitación especificado no es válido: " + tipoPrep);
        }
    }

    @Transactional(readOnly = true)
    public List<IndicadorFechaDataProjection> getAllDataFecha(Integer anio, String tipoPrep) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        if ("prep1".equalsIgnoreCase(tipoPrep)) {
            return indicadorDhSequiaRepository.getAllDataFechaPrep1(startYear, startMonth, endYear, endMonth);
        } else if ("prep3".equalsIgnoreCase(tipoPrep)) {
            return indicadorDhSequiaRepository.getAllDataFechaPrep3(startYear, startMonth, endYear, endMonth);
        } else {
            throw new IllegalArgumentException("El tipo de precipitación especificado no es válido: " + tipoPrep);
        }
    }
}