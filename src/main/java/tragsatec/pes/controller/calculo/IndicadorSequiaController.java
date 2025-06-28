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

    private final IndicadorSequiaService service;

    @PostMapping("/calcular")
    public ResponseEntity<String> calcularIndicador() {
        try {
            service.calcularIndicadorSequia();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
