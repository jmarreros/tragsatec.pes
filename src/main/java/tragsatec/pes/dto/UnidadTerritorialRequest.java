package tragsatec.pes.dto; // O la ubicación de tus DTOs

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnidadTerritorialRequest {
    private Integer id;
    private String codigo;
    private String nombre;
    private String tipo;
    private Boolean activo;
    private String comentario;
    private Integer demarcacion; // Demarcacion ID
}