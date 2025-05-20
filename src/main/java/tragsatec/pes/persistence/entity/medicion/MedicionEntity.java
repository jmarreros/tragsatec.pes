package tragsatec.pes.persistence.entity.medicion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
import tragsatec.pes.persistence.entity.estructura.PesEntity;

import java.time.LocalDateTime;
// Consider adding Set<ArchivoMedicionEntity> and Set<DetalleMedicionEntity> for bidirectional relationships if needed

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "medicion")
@Getter
@Setter
@NoArgsConstructor
public class MedicionEntity extends AuditInsertUpdateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pes_id", nullable = false)
    private PesEntity pes;

    @Column(name = "anio")
    private Short anio;

    @Column(name = "mes")
    private Byte mes;

    @Column(name = "fuente", length = 100)
    private String fuente;

    @Column(name = "comentario", length = 200)
    private String comentario;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "usuario_aprobacion")
    private Integer usuarioAprobacion;

    @Column(name = "eliminado")
    private Boolean eliminado = false;

     @OneToMany(mappedBy = "medicion", cascade = CascadeType.ALL, orphanRemoval = true)
     private java.util.Set<DetalleMedicionEntity> detallesMedicion;
}

