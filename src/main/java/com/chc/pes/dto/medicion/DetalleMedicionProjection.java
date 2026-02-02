package com.chc.pes.dto.medicion;

import java.math.BigDecimal;

public interface DetalleMedicionProjection {
    String getCodigo();
    String getNombre();
    BigDecimal getValor();
}