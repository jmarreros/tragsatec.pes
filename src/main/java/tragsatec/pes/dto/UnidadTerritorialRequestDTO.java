package tragsatec.pes.dto; // O la ubicación de tus DTOs

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UnidadTerritorialRequestDTO {
    private Integer id;
    private String codigo;
    private String nombre;
    private String tipo;
    private Boolean activo;
    private Integer demarcacionId;
    private String comentario;
    private List<Integer> estacionesIds;
}