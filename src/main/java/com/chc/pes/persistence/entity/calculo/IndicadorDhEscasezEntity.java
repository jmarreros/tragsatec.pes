package com.chc.pes.persistence.entity.calculo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "indicador_dh_escasez")
@Getter
@Setter
@NoArgsConstructor
public class IndicadorDhEscasezEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "anio", nullable = false)
    private Short anio;

    @Column(name = "mes", nullable = false)
    private Byte mes;

    @Column(name = "dato", precision = 12, scale = 8, nullable = false)
    private BigDecimal dato;

    @Column(name = "ie", precision = 12, scale = 8, nullable = false)
    private BigDecimal ie;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "medicion_id", nullable = false)
    private Integer medicionId;

    @Column(name = "demarcacion_id", nullable = false)
    private Integer demarcacionId;
}
