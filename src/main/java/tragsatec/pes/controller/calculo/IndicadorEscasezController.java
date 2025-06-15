package tragsatec.pes.controller.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.calculo.IndicadorEscasezEntity;
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
    private final IndicadorDhEscasezService indicadorDhEscasezService;
    private final IndicadorUtEscasezService indicadorUtEscasezService;

    @PostMapping("/calcular")
    public ResponseEntity<String> calcularIndicador() {
        try {
            service.calcularIndicadorEscasez();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    // Temporal pruebas
    @PostMapping("/calcular-ut")
    public ResponseEntity<String> calcularIndicadorUtSequia() {
        try {
            indicadorUtEscasezService.calcularYGuardarIndicadoresUtEscasez(233, 1);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // Temporal pruebas
    @PostMapping("/calcular-dh")
    public ResponseEntity<String> calcularIndicadorDhSequia() {
        try {
            indicadorDhEscasezService.calcularYGuardarIndicadoresDhEscasez(233, 1);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
