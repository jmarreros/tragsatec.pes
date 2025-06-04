package tragsatec.pes.dto.medicion;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
public class MedicionDTO {
    private Integer id;
    private Integer pesId;
    private Short anio;
    private Byte mes;
    private Character tipo; // E=Escasez, S=Sequia
    private String fuente;
    private String comentario;
    private LocalDateTime fechaAprobacion;
    private Integer usuarioAprobacion;
    private Boolean eliminado;
    private Boolean procesado;
    private Set<DetalleMedicionDTO> detallesMedicion;
}