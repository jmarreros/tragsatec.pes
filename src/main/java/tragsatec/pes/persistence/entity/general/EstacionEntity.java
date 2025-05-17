package tragsatec.pes.persistence.entity.general;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
import java.util.Set;

@Entity
@Table(name = "estacion")
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Getter
@Setter
@NoArgsConstructor
public class EstacionEntity extends AuditInsertUpdateEntity {
    @Id
    @Column(length = 5)
    private String id;

    @Column(length = 100)
    private String nombre;

    @Column(length = 50)
    private String tipo;

    @Column(length = 100)
    private String fuente;

    @Column(name = "calidad_dato", length = 1)
    private Character calidadDato;

    @Column(name = "codigo_sincronizacion", length = 100)
    private String codigoSincronizacion;

    @Column(length = 50)
    private String provincia;

    @Column
    private Boolean activo;

    @Column(length = 500)
    private String comentario;

    @OneToMany(mappedBy = "estacion")
    private Set<EstacionUtEntity> estacionesUt;
}

