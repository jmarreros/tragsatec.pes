package com.chc.pes.persistence.entity.general;

    import com.fasterxml.jackson.annotation.JsonIdentityInfo; // Importar
    import com.fasterxml.jackson.annotation.ObjectIdGenerators; // Importar
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import org.springframework.data.jpa.domain.support.AuditingEntityListener;
    import com.chc.pes.persistence.audit.AuditInsertUpdateEntity;
    import java.util.Set;

    @Entity
    @Table(name = "unidad_territorial")
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

        @Column(nullable = false, length = 20)
        private String codigo;

        @Column(length = 50)
        private String codigo_dh;

        @Column(length = 100)
        private String nombre;

        @Column(length = 1, nullable = false)
        private Character tipo; // E=Escasez, S=Sequia

        @Column(length = 12)
        private String competencia;

        @Column
        private Boolean activo = true;

        @Column(length = 500)
        private String comentario;

        @Column(length = 250)
        private String imagen;

        @Column(name = "orden")
        private Integer orden;

        @ManyToOne
        @JoinColumn(name = "demarcacion_id", nullable = false)
        private DemarcacionEntity demarcacion;

        @OneToMany(mappedBy = "unidadTerritorial")
        private Set<EstacionUtEntity> estacionesUt;
    }
