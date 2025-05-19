package tragsatec.pes.dto.estructura;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PesDemarcacionUtRequestDTO {
    private Integer unidadTerritorialId;
    private Integer demarcacionId;
    private Integer pesId;
    private BigDecimal coeficiente;
}
