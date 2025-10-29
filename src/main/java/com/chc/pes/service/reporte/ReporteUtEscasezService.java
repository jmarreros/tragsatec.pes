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
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph paragraph = paragraphs.get(i);
                String texto = paragraph.getText();

                reemplazarTextoEnParagrafo(paragraph, "<<mes>>", nombreMes);
                reemplazarTextoEnParagrafo(paragraph, "<<año>>", String.valueOf(anio));
            }

            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            reemplazarTextoEnParagrafo(paragraph, "<<mes>>", nombreMes);
                            reemplazarTextoEnParagrafo(paragraph, "<<año>>", String.valueOf(anio));
                        }
                    }
                }
            }

            // Agregar salto de página antes del nuevo contenido
            XWPFParagraph saltoPagina = document.createParagraph();
            XWPFRun runSalto = saltoPagina.createRun();
            runSalto.addBreak(BreakType.PAGE);

            // Crear nuevo ítem para el índice
            XWPFParagraph tituloIndice = document.createParagraph();
            tituloIndice.setStyle("Heading2"); // Heading2 para sub-ítem (2.1)
            XWPFRun runTitulo = tituloIndice.createRun();
            runTitulo.setText("2.1 UT01 Occidente");
            runTitulo.setBold(true);
            runTitulo.setFontSize(14);

            // Crear tablas debajo del título
            XWPFTable table = document.createTable(5, 4);
            crearTablaPrincipal(table);

            document.createParagraph(); // Espacio

            XWPFTable table1 = document.createTable(5, 4);
            crearTablaPrincipal(table1);

            XWPFParagraph saltoPagina_horizontal = document.createParagraph();
            configurarOrientacionHorizontal(saltoPagina_horizontal);

            XWPFTable table2 = document.createTable(5, 4);
            crearTablaPrincipal(table2);


            try (FileOutputStream out = new FileOutputStream(archivoFinal)) {
                document.write(out);
            }
        }
    }


    private void configurarOrientacionHorizontal(XWPFParagraph paragraph) {
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr ppr = paragraph.getCTP().getPPr();
        if (ppr == null) {
            ppr = paragraph.getCTP().addNewPPr();
        }

        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr sectPr = ppr.getSectPr();
        if (sectPr == null) {
            sectPr = ppr.addNewSectPr();
        }

        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz pageSize = sectPr.getPgSz();
        if (pageSize == null) {
            pageSize = sectPr.addNewPgSz();
        }

        // Establecer orientación horizontal (landscape)
        pageSize.setOrient(org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation.LANDSCAPE);

        // Intercambiar ancho y alto para orientación horizontal
        // Tamaño carta: 11906 x 15840 (ancho x alto en vertical)
        pageSize.setW(java.math.BigInteger.valueOf(15840)); // Ancho en horizontal
        pageSize.setH(java.math.BigInteger.valueOf(11906)); // Alto en horizontal
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



    private static void crearTablaPrincipal(XWPFTable table) {
        // Configurar bordes de la tabla
        table.setWidth("100%");

        // FILA 0: Cabecera principal con colspan
        XWPFTableRow row0 = table.getRow(0);
        XWPFTableCell cell00 = row0.getCell(0);
        cell00.setText("Reporte de Ventas 2025");
        cell00.setColor("4472C4"); // Color de fondo azul

        // Fusionar las 4 columnas de la primera fila (colspan=4)
        cell00.getCTTc().addNewTcPr().addNewGridSpan().setVal(java.math.BigInteger.valueOf(4));

        // Centrar texto
        XWPFParagraph p0 = cell00.getParagraphs().get(0);
        p0.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r0 = p0.getRuns().isEmpty() ? p0.createRun() : p0.getRuns().get(0);
        r0.setBold(true);
        r0.setColor("FFFFFF");

        // Eliminar celdas fusionadas
        row0.removeCell(1);
        row0.removeCell(1);
        row0.removeCell(1);

        // FILA 1: Subcabeceras
        XWPFTableRow row1 = table.getRow(1);
        configurarCeldaCabecera(row1.getCell(0), "Región");
        configurarCeldaCabecera(row1.getCell(1), "Producto");
        configurarCeldaCabecera(row1.getCell(2), "Cantidad");
        configurarCeldaCabecera(row1.getCell(3), "Total");

        // FILA 2: Primera región con rowspan
        XWPFTableRow row2 = table.getRow(2);
        XWPFTableCell cellNorte = row2.getCell(0);
        cellNorte.setText("Norte");

        // Fusionar 2 filas (rowspan=2)
        cellNorte.getCTTc().addNewTcPr().addNewVMerge().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.RESTART);

        row2.getCell(1).setText("Laptop");
        row2.getCell(2).setText("150");
        row2.getCell(3).setText("$225,000");

        // FILA 3: Continuación región Norte
        XWPFTableRow row3 = table.getRow(3);
        XWPFTableCell cellNorte2 = row3.getCell(0);
        cellNorte2.getCTTc().addNewTcPr().addNewVMerge().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.CONTINUE);

        row3.getCell(1).setText("Mouse");
        row3.getCell(2).setText("500");
        row3.getCell(3).setText("$15,000");

        // FILA 4: Segunda región
        XWPFTableRow row4 = table.getRow(4);
        row4.getCell(0).setText("Sur");
        row4.getCell(1).setText("Teclado");
        row4.getCell(2).setText("300");
        row4.getCell(3).setText("$45,000");
    }

    private static void configurarCeldaCabecera(XWPFTableCell cell, String texto) {
        cell.setText(texto);
        cell.setColor("D9E1F2"); // Color de fondo gris claro

        XWPFParagraph p = cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);

        if (!p.getRuns().isEmpty()) {
            XWPFRun run = p.getRuns().get(0);
            run.setBold(true);
        }
    }


}