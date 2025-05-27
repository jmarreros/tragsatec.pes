package tragsatec.pes.dto.general;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UnidadTerritorialResponseDTO {
    private Integer id;
    private String codigo;
    private String nombre;
    private String comentario;
    private String imagen;
    private DemarcacionSummaryDTO demarcacion;
    private List<EstacionSummaryDTO> estaciones;
}
