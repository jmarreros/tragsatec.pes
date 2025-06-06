package tragsatec.pes.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app.constants")
@Getter
@Setter
// Buscar las constantes de la aplicación en application.properties
public class AppConstants {
    private BigDecimal factorPrealerta;
    private BigDecimal factorAlerta;
    private BigDecimal factorEmergencia;
}