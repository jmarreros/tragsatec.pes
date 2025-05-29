package tragsatec.pes.persistence.entity.estructura;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
import tragsatec.pes.persistence.entity.general.EstacionEntity; // Nueva importación
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity; // Nueva importación

import java.math.BigDecimal;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "pes_umbral_sequia")
@Getter
@Setter
@NoArgsConstructor
public class PesUmbralSequiaEntity extends AuditInsertUpdateEntity {

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

    @Column(name = "mes")
    private Byte mes;

    @Column(name = "promedio_prep_1", precision = 12, scale = 8)
    private BigDecimal promedioPrep1;

    @Column(name = "promedio_prep_3", precision = 12, scale = 8)
    private BigDecimal promedioPrep3;

    @Column(name = "promedio_prep_6", precision = 12, scale = 8)
    private BigDecimal promedioPrep6;

    @Column(name = "max_prep_1", precision = 12, scale = 8)
    private BigDecimal maxPrep1;

    @Column(name = "max_prep_3", precision = 12, scale = 8)
    private BigDecimal maxPrep3;

    @Column(name = "max_prep_6", precision = 12, scale = 8)
    private BigDecimal maxPrep6;

    @Column(name = "min_prep_1", precision = 12, scale = 8)
    private BigDecimal minPrep1;

    @Column(name = "min_prep_3", precision = 12, scale = 8)
    private BigDecimal minPrep3;

    @Column(name = "min_prep_6", precision = 12, scale = 8)
    private BigDecimal minPrep6;

    @Column(name = "desv_prep_1", precision = 12, scale = 8)
    private BigDecimal desvPrep1;

    @Column(name = "desv_prep_3", precision = 12, scale = 8)
    private BigDecimal desvPrep3;

    @Column(name = "desv_prep_6", precision = 12, scale = 8)
    private BigDecimal desvPrep6;
}
