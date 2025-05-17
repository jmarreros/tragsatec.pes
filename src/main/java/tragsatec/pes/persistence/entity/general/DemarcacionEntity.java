package tragsatec.pes.persistence.entity.general;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
import java.util.Set;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "demarcacion", uniqueConstraints = @UniqueConstraint(name = "unique_codigo_demarcacion", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class DemarcacionEntity extends AuditInsertUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 4, unique = true)
    private String codigo;

    @Column(length = 100)
    private String nombre;

    @Column
    private Boolean activo;

    @Column(length = 500)
    private String comentario;

    @OneToMany(mappedBy = "demarcacion")
    @JsonManagedReference
    private Set<UnidadTerritorialEntity> unidadesTerritoriales;
}

