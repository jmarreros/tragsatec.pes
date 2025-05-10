package tragsatec.pes.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditOnlyInsertEntity;

@Entity
@Table(name = "[archivo_medicion]")
@EntityListeners({AuditingEntityListener.class, AuditOnlyInsertEntity.class})
@Setter
@Getter
@NoArgsConstructor
public class FileMeasurement extends AuditOnlyInsertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 512)
    private String filePath;

    @Column(nullable = false, length = 250)
    private String fileName;

    @Column(nullable = false)
    private byte month;

    @Column(nullable = false)
    private short year;
}
