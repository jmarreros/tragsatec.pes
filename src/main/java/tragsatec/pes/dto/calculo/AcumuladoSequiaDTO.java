package tragsatec.pes.dto.calculo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Getter
public class AcumuladoSequiaDTO {
    private Integer estacionId;
    private BigDecimal Pre3;
    private BigDecimal Pre6;
}
