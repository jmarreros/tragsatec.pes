package tragsatec.pes.persistence.entity.estructura;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;

import java.math.BigDecimal;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "pes_demarcacion_ut",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"unidad_territorial_id", "demarcacion_id", "pes_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PesDemarcacionUtEntity extends AuditInsertUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "unidad_territorial_id", nullable = false)
    private Integer unidadTerritorialId;

    @Column(name = "demarcacion_id", nullable = false)
    private Integer demarcacionId;

    @Column(name = "coeficiente", nullable = false, precision = 5, scale = 2)
    private BigDecimal coeficiente;

    @ManyToOne
    @JoinColumn(name = "pes_id", nullable = false)
    private PesEntity pes;
}