package tragsatec.pes.persistence.entity.estructura;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;

import java.math.BigDecimal;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "pes_ut_estacion",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"unidad_territorial_id", "estacion_id", "pes_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PesUtEstacionEntity extends AuditInsertUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacion_id", nullable = false)
    private EstacionEntity estacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_territorial_id", nullable = false)
    private UnidadTerritorialEntity unidadTerritorial;

    @Column(name = "coeficiente", nullable = false, precision = 5, scale = 2)
    private BigDecimal coeficiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pes_id", nullable = false)
    private PesEntity pes;
}