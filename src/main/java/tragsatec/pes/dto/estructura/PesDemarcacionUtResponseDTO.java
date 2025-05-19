package tragsatec.pes.dto.estructura; // Asegúrate de que el paquete sea el correcto

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PesDemarcacionUtResponseDTO {
    private Integer id;
    private Integer unidadTerritorialId;
    private Integer demarcacionId;
    private Integer pesId;
    private BigDecimal coeficiente;
}