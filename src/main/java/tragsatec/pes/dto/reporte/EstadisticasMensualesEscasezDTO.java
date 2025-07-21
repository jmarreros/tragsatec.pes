package tragsatec.pes.dto.reporte;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EstadisticasMensualesEscasezDTO {
    private Integer mes;
    private BigDecimal media;
    private BigDecimal mediana;
    private BigDecimal maximo;
    private BigDecimal minimo;
    private BigDecimal desviacionEstandar;
    private BigDecimal xemerg;
    private BigDecimal xpre;
    private BigDecimal xmax;
    private BigDecimal xmin;
}