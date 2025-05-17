package tragsatec.pes.persistence.entity.general;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "estacion_ut")
public class EstacionUtEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "estacion_id", nullable = false)
    private EstacionEntity estacion;

    @ManyToOne
    @JoinColumn(name = "unidad_territorial_id", nullable = false)
    private UnidadTerritorialEntity unidadTerritorial;
}

