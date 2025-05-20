package tragsatec.pes.dto.estructura;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PesUmbralSequiaDTO {
    private Integer id;
    private Integer pesId;
    private Integer estacionId;
    private Integer unidadTerritorialId;
    private Byte mes;
    private BigDecimal promedioPrep1;
    private BigDecimal promedioPrep3;
    private BigDecimal promedioPrep6;
    private BigDecimal maxPrep1;
    private BigDecimal maxPrep3;
    private BigDecimal maxPrep6;
    private BigDecimal minPrep1;
    private BigDecimal minPrep3;
    private BigDecimal minPrep6;
    private BigDecimal desvPrep1;
    private BigDecimal desvPrep3;
    private BigDecimal desvPrep6;
}

