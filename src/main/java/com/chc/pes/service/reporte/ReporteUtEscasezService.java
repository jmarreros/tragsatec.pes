package com.chc.pes.service.reporte;

import com.chc.pes.dto.general.DemarcacionProjection;
import com.chc.pes.service.general.DemarcacionService;
import com.chc.pes.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;
import com.chc.pes.service.estructura.PesService;

import org.apache.poi.xwpf.usermodel.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

@Service
public class ReporteUtEscasezService {

    @Value("${file.report-dir}")
    private String reportDir;
    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;
    private final PesService pesService;
    private final DemarcacionService demarcacionService;

    @Autowired
    public ReporteUtEscasezService(IndicadorUtEscasezRepository indicadorUtEscasezRepository, PesService pesService, DemarcacionService demarcacionService) {
        this.indicadorUtEscasezRepository = indicadorUtEscasezRepository;
        this.demarcacionService = demarcacionService;
        this.pesService = pesService;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer utId) {
        return indicadorUtEscasezRepository.getAllDataIndicadorAnioMes(utId);
    }

    @Transactional(readOnly = true)
    public List<IndicadorFechaDataProjection> getAllDataFecha(Integer anio) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;
        return indicadorUtEscasezRepository.getAllDataFecha(startYear, startMonth, endYear, endMonth);
    }

    @Transactional(readOnly = true)
    public List<IndicadorDemarcacionFechaDataProjection> getAllDataFechaDemarcacion(Integer demarcacionId, Integer anio) {
        Integer pesId = pesService.findActiveAndApprovedPesId()
                .orElseThrow(() -> new RuntimeException("No se encontró ningún PES activo y aprobado."));

        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorUtEscasezRepository.getAllDataFechaDemarcacion(pesId, demarcacionId, startYear, startMonth, endYear, endMonth);
    }

    @Transactional(readOnly = true)
    public List<IndicadorUTFechaDataProjection> getTotalDataUTFecha(Integer utId, Integer anio) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorUtEscasezRepository.getTotalDataUTFecha(utId, startYear, startMonth, endYear, endMonth);
    }

    @Transactional(readOnly = true)
    public List<IndicadorUTFechaDataProjection> getUTEstacionFecha(Integer utId, Integer anio) {
        Integer pesId = pesService.findActiveAndApprovedPesId()
                .orElseThrow(() -> new RuntimeException("No se encontró ningún PES activo y aprobado."));

        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorUtEscasezRepository.getUTEstacionFecha(pesId, utId, startYear, startMonth, endYear, endMonth);
    }


    public void generarReporteWord(Integer anio, Integer mes, String tipo) throws IOException {
        String archivoOrigen = reportDir + "/UTE_" + tipo + ".docx";
        String archivoFinal = reportDir + "/Reporte_UTE_" + tipo + ".docx";

        String nombreMes = DateUtil.obtenerNombreMesCapitalizado(mes);

        try (XWPFDocument document = new XWPFDocument(new FileInputStream(archivoOrigen))) {
            // Reemplazar en párrafos
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                reemplazarTextoEnParagrafo(paragraph, "<<mes>>", String.valueOf(nombreMes));
                reemplazarTextoEnParagrafo(paragraph, "<<año>>", String.valueOf(anio));
            }

            // Reemplazar en tablas
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            reemplazarTextoEnParagrafo(paragraph, "<<mes>>", String.valueOf(nombreMes));
                            reemplazarTextoEnParagrafo(paragraph, "<<año>>", String.valueOf(anio));
                        }
                    }
                }
            }

            // Guardar documento modificado
            try (FileOutputStream out = new FileOutputStream(archivoFinal)) {
                document.write(out);
            }
        }
    }

    private void reemplazarTextoEnParagrafo(XWPFParagraph paragraph, String buscar, String reemplazar) {
        String textoCompleto = paragraph.getText();
        if (textoCompleto.contains(buscar)) {
            // Construir texto completo de todos los runs
            StringBuilder sb = new StringBuilder();
            for (XWPFRun run : paragraph.getRuns()) {
                sb.append(run.getText(0));
            }
            String nuevoTexto = sb.toString().replace(buscar, reemplazar);

            // Limpiar runs existentes excepto el primero
            for (int i = paragraph.getRuns().size() - 1; i > 0; i--) {
                paragraph.removeRun(i);
            }

            // Establecer nuevo texto en el primer run manteniendo formato
            XWPFRun primerRun = paragraph.getRuns().get(0);
            primerRun.setText(nuevoTexto, 0);
        }
    }



    private void generarTablaDemarcacionUT(String tipo, Integer anio) {
        List<DemarcacionProjection> demarcacionesEscasez = demarcacionService.findDemarcacionesByTipo('E');
        // Buscar en la lista de demarcaciones en el campo de nombre que tenga el texto: "oriental" u "occidental" según el tipo
        DemarcacionProjection demarcacionTipo = demarcacionesEscasez.stream()
                .filter(d -> d.getNombre().toLowerCase().contains(tipo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la demarcación de tipo: " + tipo));
        Integer demarcacionId = demarcacionTipo.getId();

        // Tabla general de datos por demarcación
        List<IndicadorDemarcacionFechaDataProjection> datos = getAllDataFechaDemarcacion(demarcacionId, anio);
    }
}