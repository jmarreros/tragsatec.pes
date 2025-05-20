package tragsatec.pes.dto.medicion;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DetalleMedicionDTO {
    private Integer id;
    private Integer medicionId;
    private BigDecimal valor;
    private String tipoDato;
    private Integer estacionId;
    private Integer unidadTerritorialId;
    private Integer demarcacionId;
}

