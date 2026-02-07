package com.chc.pes.dto.medicion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para representar la previsualización de datos obtenidos desde FTP.
 * Contiene la información del periodo y los datos de medición.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrevisualizacionFTPDTO {
    private Short anio;
    private Byte mes;
    private Character tipo;
    private String nombreArchivo;
    private List<MedicionDatoDTO> datos;
    private int totalRegistros;
}

