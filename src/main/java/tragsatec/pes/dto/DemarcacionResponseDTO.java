package tragsatec.pes.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DemarcacionResponseDTO {
    private Integer id;
    private String codigo;
    private String nombre;
    private List<UnidadTerritorialSummaryDTO> unidadesTerritoriales;
}
