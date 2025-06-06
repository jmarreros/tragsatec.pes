package tragsatec.pes.controller.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.calculo.IndicadorEscasezEntity;
import tragsatec.pes.service.calculo.IndicadorEscasezService;

import java.util.List;

@RestController
@RequestMapping("/indicadores/escasez")
@RequiredArgsConstructor
public class IndicadorEscasezController {

    private final IndicadorEscasezService service;

    @PostMapping("/calcular")
    public ResponseEntity<IndicadorEscasezEntity> calcularIndicador(
            @RequestParam Integer estacionId,
            @RequestParam Short anio,
            @RequestParam Byte mes) {

        IndicadorEscasezEntity resultado = service.calcularIndicadorEscasez(estacionId, anio, mes);
        return ResponseEntity.ok(resultado);
    }
}
