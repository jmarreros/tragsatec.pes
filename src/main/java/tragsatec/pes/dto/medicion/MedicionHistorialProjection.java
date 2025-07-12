package tragsatec.pes.dto.medicion;

import java.time.LocalDateTime;

public interface MedicionHistorialProjection {
    Integer getId();
    Short getAnio();
    Byte getMes();
    Boolean getProcesado();
    Boolean getEliminado();
    Integer getFileId();
    String getFileName();
    String getCreatedBy();
    LocalDateTime getCreatedAt();
}