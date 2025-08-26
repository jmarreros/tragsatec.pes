package com.chc.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.calculo.IndicadorFechaDataProjection;
import com.chc.pes.persistence.repository.calculo.IndicadorDhEscasezRepository;

import java.util.List;

@Service
public class ReporteDhEscasezService {

    private final IndicadorDhEscasezRepository indicadorDhEscasezRepository;

    @Autowired
    public ReporteDhEscasezService(IndicadorDhEscasezRepository indicadorDhEscasezRepository) {
        this.indicadorDhEscasezRepository = indicadorDhEscasezRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer demarcacionId) {
        return indicadorDhEscasezRepository.getAllDataIndicadorAnioMes(demarcacionId);
    }

    @Transactional(readOnly = true)
    public List<IndicadorFechaDataProjection> getAllDataFecha(Integer anio) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorDhEscasezRepository.getAllDataFecha(startYear, startMonth, endYear, endMonth);
    }
}