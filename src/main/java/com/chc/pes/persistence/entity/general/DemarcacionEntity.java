package com.chc.pes.persistence.entity.general;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.chc.pes.persistence.audit.AuditInsertUpdateEntity;
import java.util.Set;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "demarcacion")
@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DemarcacionEntity extends AuditInsertUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 5)
    private String codigo;

    @Column(length = 100)
    private String nombre;

    @Column(nullable = false, length = 1)
    private Character tipo;

    @Column(length = 250)
    private String imagen;

    @Column
    private Boolean activo = true;

    @Column(length = 500)
    private String comentario;

    @OneToMany(mappedBy = "demarcacion")
    private Set<UnidadTerritorialEntity> unidadesTerritoriales;
}
