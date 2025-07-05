package tragsatec.pes.controller.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.service.calculo.IndicadorDhSequiaService;
import tragsatec.pes.service.calculo.IndicadorSequiaService;
import tragsatec.pes.service.calculo.IndicadorUtSequiaService;

@RestController
@RequestMapping("/indicadores/sequia")
@RequiredArgsConstructor
public class IndicadorSequiaController {

    private final IndicadorSequiaService indicadorSequiaService;

    @PostMapping("/calcular")
    public ResponseEntity<String> calcularIndicador() {
        try {
            indicadorSequiaService.calcularIndicadorSequia();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/medicion/no-procesado")
    public ResponseEntity<Void> limpiarIndicadoresMedicionNoProcesada() {
        try {
            indicadorSequiaService.limpiarIndicadoresMedicionNoProcesada();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
