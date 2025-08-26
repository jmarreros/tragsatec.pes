package com.chc.pes.persistence.entity.estructura;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.chc.pes.persistence.audit.AuditInsertUpdateEntity;
import com.chc.pes.persistence.entity.general.EstacionEntity;
import com.chc.pes.persistence.entity.general.UnidadTerritorialEntity;

import java.math.BigDecimal;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "pes_umbral_escasez")
@Getter
@Setter
@NoArgsConstructor
public class PesUmbralEscasezEntity extends AuditInsertUpdateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pes_id", nullable = false)
    private PesEntity pes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacion_id", nullable = false)
    private EstacionEntity estacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_territorial_id", nullable = false)
    private UnidadTerritorialEntity unidadTerritorial;

    @Column(name = "escenario", length = 20)
    private String escenario;

    @Column(name = "estadistico", length = 10)
    private String estadistico;

    @Column(name = "param", length = 10)
    private String param;

    @Column(name = "mes_10", precision = 12, scale = 8)
    private BigDecimal mes10;

    @Column(name = "mes_11", precision = 12, scale = 8)
    private BigDecimal mes11;

    @Column(name = "mes_12", precision = 12, scale = 8)
    private BigDecimal mes12;

    @Column(name = "mes_1", precision = 12, scale = 8)
    private BigDecimal mes1;

    @Column(name = "mes_2", precision = 12, scale = 8)
    private BigDecimal mes2;

    @Column(name = "mes_3", precision = 12, scale = 8)
    private BigDecimal mes3;

    @Column(name = "mes_4", precision = 12, scale = 8)
    private BigDecimal mes4;

    @Column(name = "mes_5", precision = 12, scale = 8)
    private BigDecimal mes5;

    @Column(name = "mes_6", precision = 12, scale = 8)
    private BigDecimal mes6;

    @Column(name = "mes_7", precision = 12, scale = 8)
    private BigDecimal mes7;

    @Column(name = "mes_8", precision = 12, scale = 8)
    private BigDecimal mes8;

    @Column(name = "mes_9", precision = 12, scale = 8)
    private BigDecimal mes9;
}
