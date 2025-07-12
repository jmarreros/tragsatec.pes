package tragsatec.pes.persistence.entity.medicion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditOnlyInsertEntity;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditOnlyInsertEntity.class})
@Table(name = "archivo_medicion")
@Getter
@Setter
@NoArgsConstructor
public class ArchivoMedicionEntity extends AuditOnlyInsertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 250)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicion_id", nullable = false)
    private MedicionEntity medicion;
}