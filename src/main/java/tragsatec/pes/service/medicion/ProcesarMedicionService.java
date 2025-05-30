package tragsatec.pes.service.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.exception.PesNoValidoException;
import tragsatec.pes.service.estructura.PesService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcesarMedicionService {
    private final PesService pesService;

    public void procesarArchivoMedicion(Character tipo, Short anio, Byte mes, MultipartFile file) {
        // 1- Detectar el Plan Especial de Sequia (PES) actual
        Optional<Integer> pesIdOptional = pesService.findActiveAndApprovedPesId();

        if (pesIdOptional.isEmpty()) {
            throw new PesNoValidoException("No hay un Plan Especial de Sequía (PES) activo y aprobado.");
        }
        // 2- Revisar la data del archivo de medicion

    }
}