package tragsatec.pes.dto.medicion;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArchivoMedicionDTO {
    private Integer id;
    private String fileName;
    private String filePath;
    private Integer medicionId;
    private Boolean activo;
}

