package tragsatec.pes.persistence.entity.general;

    import com.fasterxml.jackson.annotation.JsonBackReference;
    import com.fasterxml.jackson.annotation.JsonIdentityInfo; // Importar
    import com.fasterxml.jackson.annotation.ObjectIdGenerators; // Importar
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import org.springframework.data.jpa.domain.support.AuditingEntityListener;
    import tragsatec.pes.persistence.audit.AuditInsertUpdateEntity;
    import java.util.Set;

    @Entity
    @Table(name = "unidad_territorial", uniqueConstraints = @UniqueConstraint(name = "unique_codigo_ut", columnNames = "codigo"))
    @EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public class UnidadTerritorialEntity extends AuditInsertUpdateEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(nullable = false)
        private Integer id;

        @Column(nullable = false, length = 20,  unique = true)
        private String codigo;

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
        private DemarcacionEntity demarcacion;

        @OneToMany(mappedBy = "unidadTerritorial")
        private Set<EstacionUtEntity> estacionesUt;
    }