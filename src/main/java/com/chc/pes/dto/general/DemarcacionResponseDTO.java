package com.chc.pes.dto.general;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DemarcacionResponseDTO {
    private Integer id;
    private String codigo;
    private String nombre;
    private Character tipo;
    private String imagen;
    private List<UnidadTerritorialSummaryDTO> unidadesTerritoriales;
}

