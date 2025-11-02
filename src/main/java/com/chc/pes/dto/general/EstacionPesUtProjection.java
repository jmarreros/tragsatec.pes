package com.chc.pes.dto.general;

import java.math.BigDecimal;

public interface EstacionPesUtProjection {
    Integer getId();
    String getCodigo();
    String getNombre();
    BigDecimal getCoeficiente();
    String getCoordenadas();
}
