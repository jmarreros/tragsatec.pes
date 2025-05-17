package tragsatec.pes.persistence.entity.general;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
import java.util.Set;

@Entity
@Table(name = "unidad_territorial")
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadTerritorialEntity extends AuditInsertUpdateEntity {
    @Id
    @Column(length = 20)
    private String id;

    @Column(length = 100)
    private String nombre;

    @Column(length = 3)
    private String tipo;

    @Column
    private Boolean activo;

    @Column(length = 500)
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "demarcacion_id", nullable = false)
    @JsonBackReference
    private DemarcacionEntity demarcacion;

    @OneToMany(mappedBy = "unidadTerritorial")
    private Set<EstacionUtEntity> estacionesUt;
}

