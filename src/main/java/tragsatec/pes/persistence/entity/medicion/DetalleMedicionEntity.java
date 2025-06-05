package tragsatec.pes.persistence.entity.medicion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
import tragsatec.pes.persistence.entity.general.EstacionEntity;

import java.math.BigDecimal;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "detalle_medicion")
@Getter
@Setter
@NoArgsConstructor
public class DetalleMedicionEntity extends AuditInsertUpdateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicion_id", nullable = false)
    private MedicionEntity medicion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacion_id", nullable = false)
    private EstacionEntity estacion;

    @Column(name = "valor", precision = 12, scale = 8)
    private BigDecimal valor;

    @Column(name = "tipo_dato", length = 20)
    private String tipoDato;
}