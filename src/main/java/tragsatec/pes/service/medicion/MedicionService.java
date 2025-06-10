package tragsatec.pes.service.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.medicion.MedicionDTO;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.persistence.entity.estructura.PesEntity;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;
import tragsatec.pes.persistence.entity.medicion.DetalleMedicionEntity;
import tragsatec.pes.persistence.repository.general.EstacionRepository;
import tragsatec.pes.persistence.repository.medicion.MedicionRepository;
import tragsatec.pes.service.estructura.PesService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class MedicionService {

    private final MedicionRepository medicionRepository;
    private final PesService pesService;
    private final EstacionRepository estacionRepository;
    private final DetalleMedicionService detalleMedicionService;

    private DetalleMedicionEntity mapDetalleDTOToEntity(DetalleMedicionDTO detalleDTO, MedicionEntity medicion) {
        if (detalleDTO == null) return null;
        DetalleMedicionEntity detalleEntity = new DetalleMedicionEntity();
        detalleEntity.setId(detalleDTO.getId());
        detalleEntity.setValor(detalleDTO.getValor());
        detalleEntity.setTipoDato(detalleDTO.getTipoDato());
        detalleEntity.setMedicion(medicion);
        EstacionEntity estacion = estacionRepository.findById(detalleDTO.getEstacionId()).orElse(null);
        detalleEntity.setEstacion(estacion);

        return detalleEntity;
    }

    private MedicionDTO mapToDTO(MedicionEntity entity) {
        if (entity == null) return null;

        MedicionDTO dto = new MedicionDTO();
        dto.setId(entity.getId());
        dto.setAnio(entity.getAnio());
        dto.setMes(entity.getMes());
        dto.setTipo(entity.getTipo());
        dto.setFuente(entity.getFuente());
        dto.setComentario(entity.getComentario());
        dto.setFechaAprobacion(entity.getFechaAprobacion());
        dto.setUsuarioAprobacion(entity.getUsuarioAprobacion());
        dto.setEliminado(entity.getEliminado());
        dto.setProcesado(entity.getProcesado());
        if (entity.getPes() != null) {
            dto.setPesId(entity.getPes().getId());
        }
        if (entity.getDetallesMedicion() != null) {
            dto.setDetallesMedicion(entity.getDetallesMedicion().stream()
                    .map(detalleMedicionService::mapToDTO)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    private MedicionEntity mapToEntity(MedicionDTO dto) {
        MedicionEntity entity = new MedicionEntity();

        entity.setAnio(dto.getAnio());
        entity.setMes(dto.getMes());
        entity.setTipo(dto.getTipo());
        entity.setFuente(dto.getFuente());
        entity.setComentario(dto.getComentario());
        entity.setFechaAprobacion(dto.getFechaAprobacion());
        entity.setUsuarioAprobacion(dto.getUsuarioAprobacion());
        entity.setEliminado(dto.getEliminado() != null ? dto.getEliminado() : false);
        entity.setProcesado(dto.getProcesado() != null ? dto.getProcesado() : false);

        if (dto.getPesId() != null) {
            PesEntity pes = pesService.findById(dto.getPesId())
                    .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
            entity.setPes(pes);
        }

        if (dto.getDetallesMedicion() != null) {
            entity.setDetallesMedicion(dto.getDetallesMedicion().stream()
                    .map(detalleDTO -> mapDetalleDTOToEntity(detalleDTO, entity))
                    .collect(Collectors.toSet()));
        } else {
            entity.setDetallesMedicion(new HashSet<>());
        }

        return entity;
    }

    @Transactional(readOnly = true)
    public List<MedicionDTO> findAll() {
        return medicionRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<MedicionDTO> findById(Integer id) {
        return medicionRepository.findById(id).map(this::mapToDTO);
    }

    @Transactional
    public MedicionDTO save(MedicionDTO dto) {
        MedicionEntity entity = mapToEntity(dto);
        // El campo procesado se inicializa en mapToEntity o en la propia entidad
        if (entity.getDetallesMedicion() != null) {
            entity.getDetallesMedicion().forEach(detalle -> detalle.setMedicion(entity));
        }
        MedicionEntity savedEntity = medicionRepository.save(entity);
        return mapToDTO(savedEntity);
    }

    @Transactional
    public Optional<MedicionDTO> update(Integer id, MedicionDTO dto) {
        return medicionRepository.findById(id)
                .map(existingEntity -> {
                    existingEntity.setAnio(dto.getAnio());
                    existingEntity.setMes(dto.getMes());
                    existingEntity.setTipo(dto.getTipo());
                    existingEntity.setFuente(dto.getFuente());
                    existingEntity.setComentario(dto.getComentario());
                    existingEntity.setFechaAprobacion(dto.getFechaAprobacion());
                    existingEntity.setUsuarioAprobacion(dto.getUsuarioAprobacion());
                    existingEntity.setEliminado(dto.getEliminado() != null ? dto.getEliminado() : existingEntity.getEliminado());
                    existingEntity.setProcesado(dto.getProcesado() != null ? dto.getProcesado() : existingEntity.getProcesado());

                    if (dto.getPesId() != null) {
                        PesEntity pes = pesService.findById(dto.getPesId())
                                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
                        existingEntity.setPes(pes);
                    }

                    // Manejo de la colección de detalles
                    if (dto.getDetallesMedicion() != null) {
                        existingEntity.getDetallesMedicion().clear();
                        dto.getDetallesMedicion().forEach(detalleDTO -> {
                            DetalleMedicionEntity detalleEntity = mapDetalleDTOToEntity(detalleDTO, existingEntity);
                            existingEntity.getDetallesMedicion().add(detalleEntity);
                        });
                    }

                    MedicionEntity updatedEntity = medicionRepository.save(existingEntity);
                    return mapToDTO(updatedEntity);
                });
    }

    @Transactional
    public void anularMedicionAnterior(Integer pesId, Character tipo, Short anio, Byte mes) {
        List<MedicionEntity> mediciones = medicionRepository.findByPesIdAndTipoAndAnioAndMesAndEliminadoFalse(pesId, tipo, anio, mes);
        for (MedicionEntity medicion : mediciones) {
            medicion.setEliminado(true);
            medicionRepository.save(medicion);
        }
    }


    @Transactional(readOnly = true)
    public MedicionDTO findFirstNotProcessedMedicionByTipo(Character tipo) {
        MedicionEntity medicion = medicionRepository.findFirstNotProcessedMedicionByTipo(tipo);
        return mapToDTO(medicion);
    }

    public Optional<MedicionDTO> findLastProcessedMedicionByTipo(Character tipo) {
        MedicionEntity medicion = medicionRepository.findLastProcessedMedicionByTipo(tipo);
        return Optional.ofNullable(mapToDTO(medicion));
    }

    @Transactional
    public void marcarComoProcesada(Integer medicionId) {
        medicionRepository.actualizarEstadoProcesado(medicionId, true);
    }
}