package tragsatec.pes.controller.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.calculo.IndicadorEscasezEntity;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;
import tragsatec.pes.service.calculo.IndicadorDhEscasezService;
import tragsatec.pes.service.calculo.IndicadorDhSequiaService;
import tragsatec.pes.service.calculo.IndicadorEscasezService;
import tragsatec.pes.service.calculo.IndicadorUtEscasezService;

import java.util.List;

@RestController
@RequestMapping("/indicadores/escasez")
@RequiredArgsConstructor
public class IndicadorEscasezController {

    private final IndicadorEscasezService service;

    @PostMapping("/calcular")
    public ResponseEntity<String> calcularIndicador() {
        try {
            service.calcularIndicadorEscasez();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    // TODO
//
//    @Transactional
//    public void desmarcarProcesada(Integer medicionId) {
//        MedicionEntity medicion = medicionRepository.findById(medicionId)
//                .orElseThrow(() -> new IllegalArgumentException("Medición no encontrada con ID: " + medicionId));
//
//        medicionRepository.actualizarEstadoProcesado(medicionId, false);
//
//        if (medicion.getTipo() == 'E') {
//            indicadorEscasezService.limpiarIndicadoresEscasez(medicionId);
//        } else if (medicion.getTipo() == 'S') {
//            indicadorSequiaService.limpiarIndicadoresSequia(medicionId);
//        }
//    }

}