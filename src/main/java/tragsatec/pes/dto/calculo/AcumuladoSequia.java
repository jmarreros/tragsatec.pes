package tragsatec.pes.dto.calculo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AcumuladoSequia {
    private Integer estacionId;
    private BigDecimal acumuladoPre3;
    private BigDecimal acumuladoPre6;
}
