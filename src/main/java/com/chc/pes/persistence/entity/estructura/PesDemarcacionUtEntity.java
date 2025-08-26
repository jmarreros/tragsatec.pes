package com.chc.pes.persistence.entity.estructura;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.chc.pes.persistence.audit.AuditInsertUpdateEntity;
import com.chc.pes.persistence.entity.general.DemarcacionEntity;
import com.chc.pes.persistence.entity.general.UnidadTerritorialEntity;

import java.math.BigDecimal;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "pes_demarcacion_ut")
@Getter
@Setter
@NoArgsConstructor
public class PesDemarcacionUtEntity extends AuditInsertUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_territorial_id", nullable = false)
    private UnidadTerritorialEntity unidadTerritorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demarcacion_id", nullable = false)
    private DemarcacionEntity demarcacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pes_id", nullable = false)
    private PesEntity pes;

    @Column(length = 1, nullable = false)
    private Character tipo; // E = Escasez, S = Sequía

    @Column(name = "coeficiente", nullable = false, precision = 12, scale = 8)
    private BigDecimal coeficiente;
}