package com.chc.pes.persistence.entity.estructura;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.chc.pes.persistence.audit.AuditInsertUpdateEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditInsertUpdateEntity.class})
@Table(name = "pes")
@Getter
@Setter
@NoArgsConstructor
public class PesEntity extends AuditInsertUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "nombre_interno", nullable = false, length = 100, unique = true)
    private String nombreInterno;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "inicio")
    private LocalDate inicio;

    @Column(name = "fin")
    private LocalDate fin;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "comentario", length = 500)
    private String comentario;

    @Column(name = "aprobado")
    private Boolean aprobado;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "usuario_aprobacion")
    private Integer usuarioAprobacion;
}
