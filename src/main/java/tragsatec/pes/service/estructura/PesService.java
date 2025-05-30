package tragsatec.pes.service.estructura;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.entity.estructura.PesEntity;
import tragsatec.pes.persistence.repository.estructura.PesRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PesService {
    @Autowired
    private PesRepository pesRepository;

    @Transactional(readOnly = true)
    public List<PesEntity> findAll() {
        return pesRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PesEntity> findById(Integer id) {
        return pesRepository.findById(id);
    }

    @Transactional
    public PesEntity save(PesEntity pesEntity) {
        return pesRepository.save(pesEntity);
    }

    @Transactional
    public Optional<PesEntity> update(Integer id, PesEntity pesDetails) {
        return pesRepository.findById(id)
                .map(existingPes -> {
                    if (pesDetails.getNombre() != null) existingPes.setNombre(pesDetails.getNombre());
                    if (pesDetails.getNombreInterno() != null)
                        existingPes.setNombreInterno(pesDetails.getNombreInterno());
                    if (pesDetails.getDescripcion() != null) existingPes.setDescripcion(pesDetails.getDescripcion());
                    if (pesDetails.getInicio() != null) existingPes.setInicio(pesDetails.getInicio());
                    if (pesDetails.getFin() != null) existingPes.setFin(pesDetails.getFin());
                    if (pesDetails.getActivo() != null) existingPes.setActivo(pesDetails.getActivo());
                    if (pesDetails.getComentario() != null) existingPes.setComentario(pesDetails.getComentario());
                    if (pesDetails.getAprobado() != null) existingPes.setAprobado(pesDetails.getAprobado());
                    if (pesDetails.getFechaAprobacion() != null)
                        existingPes.setFechaAprobacion(pesDetails.getFechaAprobacion());
                    if (pesDetails.getUsuarioAprobacion() != null)
                        existingPes.setUsuarioAprobacion(pesDetails.getUsuarioAprobacion());
                    return pesRepository.save(existingPes);
                });
    }

    @Transactional(readOnly = true)
    public Optional<Integer> findActiveAndApprovedPesId() {
        return pesRepository.findActiveAndApprovedPesId();
    }
}