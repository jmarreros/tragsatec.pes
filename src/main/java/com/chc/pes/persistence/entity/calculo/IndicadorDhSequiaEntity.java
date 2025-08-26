package com.chc.pes.persistence.entity.calculo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "indicador_dh_sequia")
@Getter
@Setter
@NoArgsConstructor
public class IndicadorDhSequiaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "anio", nullable = false)
    private Short anio;

    @Column(name = "mes", nullable = false)
    private Byte mes;

    @Column(name = "prep1", precision = 12, scale = 8, nullable = false)
    private BigDecimal prep1;

    @Column(name = "prep3", precision = 12, scale = 8)
    private BigDecimal prep3;

    @Column(name = "prep6", precision = 12, scale = 8)
    private BigDecimal prep6;

    @Column(name = "ie_b1", precision = 12, scale = 8)
    private BigDecimal ieB1;

    @Column(name = "ie_b3", precision = 12, scale = 8)
    private BigDecimal ieB3;

    @Column(name = "ie_b6", precision = 12, scale = 8)
    private BigDecimal ieB6;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "medicion_id", nullable = false)
    private Integer medicionId;

    @Column(name = "demarcacion_id", nullable = false)
    private Integer demarcacionId;
}
