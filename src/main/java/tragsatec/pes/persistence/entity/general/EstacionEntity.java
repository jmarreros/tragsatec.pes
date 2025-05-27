package tragsatec.pes.persistence.entity.general;

import com.fasterxml.jackson.annotation.JsonIdentityInfo; // Importar
import com.fasterxml.jackson.annotation.ObjectIdGenerators; // Importar
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
import java.util.Set;

@Entity
@Table(name = "estacion", uniqueConstraints = @UniqueConstraint(name = "unique_codigo_estacion", columnNames = "codigo"))
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class EstacionEntity extends AuditInsertUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(length = 100)
    private String nombre;

    @Column(name = "tipo_medicion",length = 50)
    private String tipoMedicion;

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