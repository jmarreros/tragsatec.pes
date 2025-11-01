package com.chc.pes.dto.general;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class EstacionResponseDTO {
    private Integer id;
    private String codigo;
    private String nombre;
    private String tipoMedicion;
    private String fuente;
    private Character calidadDato;
    private String codigoSaih;
    private String provincia;
    private Boolean activo;
    private String comentario;
    private String coordenadas;
    private List<UnidadTerritorialSummaryDTO> unidadesTerritoriales; // Lista de UTs asociadas de forma simplificada
}