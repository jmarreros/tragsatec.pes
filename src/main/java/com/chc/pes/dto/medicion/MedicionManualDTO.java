package com.chc.pes.dto.medicion;

import lombok.Data;
import java.util.List;

@Data
public class MedicionManualDTO {
    private Character tipo;
    private Short anio;
    private Byte mes;
    private List<DetalleMedicionDTO> detallesMedicion;
}