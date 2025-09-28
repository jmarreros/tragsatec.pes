package com.chc.pes.persistence.entity.calculo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.chc.pes.persistence.audit.AuditInsertUpdateEntity;
import com.chc.pes.persistence.audit.AuditOnlyInsertEntity;

import java.math.BigDecimal;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditOnlyInsertEntity.class})
@Table(name = "indicador_escasez")
@Getter
@Setter
@NoArgsConstructor
public class IndicadorEscasezEntity extends AuditInsertUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "anio", nullable = false)
    private Short anio;

    @Column(name = "mes", nullable = false)
    private Byte mes;

    @Column(name = "dato", precision = 12, scale = 8)
    private BigDecimal dato;

    @Column(name = "ie", precision = 12, scale = 8)
    private BigDecimal ie;



    @Column(name = "medicion_id", nullable = false)
    private Integer medicionId;

    @Column(name = "estacion_id", nullable = false)
    private Integer estacionId;
}

