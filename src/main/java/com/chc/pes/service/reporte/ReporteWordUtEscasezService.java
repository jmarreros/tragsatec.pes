package com.chc.pes.service.reporte;

import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.general.DemarcacionProjection;
import com.chc.pes.service.general.DemarcacionService;
import com.chc.pes.util.DateUtils;
import com.chc.pes.util.DocumentWordUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReporteWordUtEscasezService {

    @Value("${file.report-dir}")
    private String reportDir;
    private final DemarcacionService demarcacionService;
    private final ReporteUtEscasezService reporteUtEscasezService;

    public ReporteWordUtEscasezService(DemarcacionService demarcacionService, ReporteUtEscasezService reporteUtEscasezService) {
        this.demarcacionService = demarcacionService;
        this.reporteUtEscasezService = reporteUtEscasezService;
    }

    public void generarReporteWord(Integer anio, Integer mes, String tipo) throws IOException {
        String archivoOrigen = reportDir + "/UTE_" + tipo + ".docx";
        String archivoFinal = reportDir + "/Reporte_UTE_" + tipo + ".docx";

        try (XWPFDocument document = new XWPFDocument(new FileInputStream(archivoOrigen))) {

            // Cambiar mes y año en el documento
            cambiarMesYAnioEnParrafo(document, mes, anio);

            // Agregar salto de página
            DocumentWordUtils.agregarSaltoDePagina(document);

            // Crear tablas debajo del título
            XWPFTable table = document.createTable(5, 4);
            crearTablaPrincipal(table);

            DocumentWordUtils.encabezadoH2(document, "Nuevo Texto de Encabezado");

            document.createParagraph();

            XWPFTable table1 = document.createTable(5, 4);
            crearTablaPrincipal(table1);

            XWPFParagraph saltoPagina_horizontal = document.createParagraph();
            DocumentWordUtils.configurarOrientacionHorizontal(saltoPagina_horizontal);

            XWPFTable table2 = document.createTable(5, 4);
            crearTablaPrincipal(table2);

            try (FileOutputStream out = new FileOutputStream(archivoFinal)) {
                document.write(out);
            }
        }
    }


    private void cambiarMesYAnioEnParrafo(XWPFDocument document, Integer mes, Integer anio) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        String nombreMes = DateUtils.obtenerNombreMesCapitalizado(mes);
        for (XWPFParagraph paragraph : paragraphs) {
            DocumentWordUtils.reemplazarTextoEnParagrafo(paragraph, "<<mes>>", nombreMes);
            DocumentWordUtils.reemplazarTextoEnParagrafo(paragraph, "<<año>>", String.valueOf(anio));
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
        List<IndicadorDemarcacionFechaDataProjection> datos = reporteUtEscasezService.getAllDataFechaDemarcacion(demarcacionId, anio);
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
