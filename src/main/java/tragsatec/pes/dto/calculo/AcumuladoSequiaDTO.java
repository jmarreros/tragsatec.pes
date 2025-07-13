package tragsatec.pes.dto.calculo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AcumuladoSequiaDTO {
    private Integer estacionId;
    private BigDecimal pre3;
    private BigDecimal pre6;
}
