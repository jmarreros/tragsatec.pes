package com.chc.pes.dto.medicion;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DetalleMedicionDTO {
    private Long id;
    private Integer medicionId;
    private BigDecimal valor;
    private String tipoDato;
    private Integer estacionId;
}