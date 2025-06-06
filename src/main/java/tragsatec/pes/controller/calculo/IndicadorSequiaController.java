package tragsatec.pes.controller.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;
import tragsatec.pes.service.calculo.IndicadorSequiaService;

import java.util.List;

@RestController
@RequestMapping("/indicadores/sequia")
@RequiredArgsConstructor
public class IndicadorSequiaController {

    private final IndicadorSequiaService service;

    @PostMapping("/calcular")
    public ResponseEntity<IndicadorSequiaEntity> calcularIndicador(
            @RequestParam Integer estacionId,
            @RequestParam Short anio,
            @RequestParam Byte mes) {

        IndicadorSequiaEntity resultado = service.calcularIndicadorSequia(estacionId, anio, mes);
        return ResponseEntity.ok(resultado);
    }
}
