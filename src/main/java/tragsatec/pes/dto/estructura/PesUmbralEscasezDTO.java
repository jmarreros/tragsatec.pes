package tragsatec.pes.dto.estructura;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PesUmbralEscasezDTO {
    private Integer id;
    private Integer pesId;
    private Integer estacionId;
    private Integer unidadTerritorialId;
    private String escenario;
    private String estadistico;
    private String param;
    private BigDecimal mes10;
    private BigDecimal mes11;
    private BigDecimal mes12;
    private BigDecimal mes1;
    private BigDecimal mes2;
    private BigDecimal mes3;
    private BigDecimal mes4;
    private BigDecimal mes5;
    private BigDecimal mes6;
    private BigDecimal mes7;
    private BigDecimal mes8;
    private BigDecimal mes9;
}

