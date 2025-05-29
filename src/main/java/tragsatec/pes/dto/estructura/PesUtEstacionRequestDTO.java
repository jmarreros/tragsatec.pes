package tragsatec.pes.dto.estructura;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PesUtEstacionRequestDTO {
    private Integer unidadTerritorialId;
    private Integer estacionId;
    private Integer pesId;
    private Character tipo; // E = Escasez, S = Sequía
    private BigDecimal coeficiente;
}
