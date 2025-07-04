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

    private final IndicadorEscasezService indicadorEscasezService;

    @PostMapping("/calcular")
    public ResponseEntity<String> calcularIndicador() {
        try {
            indicadorEscasezService.calcularIndicadorEscasez();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @DeleteMapping("/medicion/{medicionId}/no-procesado")
    public ResponseEntity<Void> limpiarIndicadoresMedicionNoProcesada(@PathVariable Integer medicionId) {
        try {
            indicadorEscasezService.limpiarIndicadoresMedicionNoProcesada(medicionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}