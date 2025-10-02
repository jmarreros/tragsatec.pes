package com.chc.pes.service.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.dto.medicion.DetalleMedicionDTO;
import com.chc.pes.dto.medicion.DetalleMedicionProjection;
import com.chc.pes.persistence.entity.medicion.DetalleMedicionEntity;
import com.chc.pes.persistence.repository.general.EstacionRepository;
import com.chc.pes.persistence.repository.medicion.DetalleMedicionRepository;
import com.chc.pes.persistence.repository.medicion.MedicionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetalleMedicionService {

    private final DetalleMedicionRepository detalleMedicionRepository;
    private final MedicionRepository medicionRepository;
    private final EstacionRepository estacionRepository;

    public DetalleMedicionDTO mapToDTO(DetalleMedicionEntity entity) {
        if (entity == null) {
            return null;
        }
        DetalleMedicionDTO dto = new DetalleMedicionDTO();
        dto.setId(entity.getId());
        dto.setValor(entity.getValor());
        dto.setTipoDato(entity.getTipoDato());
        if (entity.getMedicion() != null) {
            dto.setMedicionId(entity.getMedicion().getId());
        }
        if (entity.getEstacion() != null) {
            dto.setEstacionId(entity.getEstacion().getId());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DetalleMedicionDTO> findByMedicionId(Integer medicionId) {
        return detalleMedicionRepository.findByMedicionId(medicionId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DetalleMedicionProjection> findReporteByMedicionId(Integer medicionId) {
        return detalleMedicionRepository.findReporteByMedicionId(medicionId);
    }


    public DetalleMedicionDTO actualizarDetalleMedicion(Integer medicionId, Integer estacionId, DetalleMedicionDTO detalleMedicionDTO) {
        DetalleMedicionEntity detalleMedicion = detalleMedicionRepository.findByMedicionIdAndEstacionId(medicionId, estacionId)
                .orElse(new DetalleMedicionEntity());

        detalleMedicion.setValor(detalleMedicionDTO.getValor());
        detalleMedicion.setTipoDato(detalleMedicionDTO.getTipoDato());
        detalleMedicion.setMedicion(medicionRepository.findById(medicionId).orElse(null));
        detalleMedicion.setEstacion(estacionRepository.findById(estacionId).orElse(null));

        DetalleMedicionEntity savedEntity = detalleMedicionRepository.save(detalleMedicion);
        return mapToDTO(savedEntity);
    }
}