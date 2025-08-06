package tragsatec.pes.service.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.dto.medicion.DetalleMedicionProjection;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.medicion.DetalleMedicionEntity;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;
import tragsatec.pes.persistence.repository.general.EstacionRepository;
import tragsatec.pes.persistence.repository.medicion.DetalleMedicionRepository;
import tragsatec.pes.persistence.repository.medicion.MedicionRepository;

import java.util.List;
import java.util.Optional;
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
}