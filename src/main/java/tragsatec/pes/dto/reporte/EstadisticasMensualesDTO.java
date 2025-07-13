package tragsatec.pes.dto.reporte;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EstadisticasMensualesDTO {
    private Integer mes;
    private BigDecimal media;
    private BigDecimal mediana;
    private BigDecimal maximo;
    private BigDecimal minimo;
    private BigDecimal desviacionEstandar;
    private BigDecimal probPre;
    private BigDecimal probAlerta;
    private BigDecimal probEmergencia;
}