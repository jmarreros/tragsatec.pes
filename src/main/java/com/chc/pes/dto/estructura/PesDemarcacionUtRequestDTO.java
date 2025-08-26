package com.chc.pes.dto.estructura;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PesDemarcacionUtRequestDTO {
    private Integer unidadTerritorialId;
    private Integer demarcacionId;
    private Integer pesId;
    private Character tipo; // E = Escasez, S = Sequía
    private BigDecimal coeficiente;
}
