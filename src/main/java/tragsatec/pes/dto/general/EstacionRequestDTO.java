package tragsatec.pes.dto.general;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class EstacionRequestDTO {
    private String codigo;
    private String nombre;
    private String tipoMedicion;
    private String fuente;
    private Character calidadDato;
    private String codigoSincronizacion;
    private String provincia;
    private Boolean activo;
    private String comentario;

    // IDs de las Unidades Territoriales a asociar con esta Estación
    private List<Integer> unidadTerritorialIds;
}