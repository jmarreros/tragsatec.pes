package tragsatec.pes.service.medicion;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProcesarMedicionService {

    public void procesarArchivoMedicion(Character tipo, Short anio, Byte mes, MultipartFile file) {
        // Implementa la lógica para procesar el archivo
        System.out.println("Procesando archivo: " + file.getOriginalFilename() + " con tipo: " + tipo + ", año: " + anio + ", mes: " + mes);
        // Aquí iría la lógica de parseo, validación y persistencia
    }
}