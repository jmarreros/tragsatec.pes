package tragsatec.pes.dto.medicion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para representar un dato de medición asociado a una estación.
 * Contiene el nombre de la estación y el valor de la medición.
 */
@Data
@AllArgsConstructor
public class MedicionDatoDTO {
    private String nombreEstacion;
    private BigDecimal valorMedicion;
}
