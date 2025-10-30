package com.chc.pes.service.reporte;

import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.dto.general.DemarcacionProjection;
import com.chc.pes.service.general.DemarcacionService;

import com.chc.pes.util.DocumentWordUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;
import java.util.Map;

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
            DocumentWordUtils.cambiarMesYAnioEnParrafo(document, mes, anio);

            // Agregar salto de página
            DocumentWordUtils.agregarSaltoDePagina(document);

            List<IndicadorUTFechaDataProjection> datos = reporteUtEscasezService.getTotalDataUTFecha(1, anio);
            List<Map<String, Object>> datosGrafico = DocumentWordUtils.prepararDatosTotalesParaGrafico(datos);

            // Imprimir los datos del gráfico (para verificación)
            for (Map<String, Object> dato : datosGrafico) {
                System.out.println(dato);
            }

            // Generar imagen del gráfico
            generarGraficoLineas(datosGrafico);

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




    public void generarGraficoLineas(List<Map<String, Object>> datosGrafico) throws IOException {
        // Crear la serie de datos
        XYSeries series = new XYSeries("Indicador");

        // Agregar datos a la serie
        for (int i = 0; i < datosGrafico.size(); i++) {
            Map<String, Object> dato = datosGrafico.get(i);
            Double valor = ((Number) dato.get("valor")).doubleValue();
            series.add(i, valor);
        }

        // Crear el dataset
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Crear el gráfico
        JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "",
                "",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );

        // Personalizar el gráfico
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Configurar el eje Y con valores específicos
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 1.0);
        rangeAxis.setTickUnit(new NumberTickUnit(0.25));

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setTickUnit(new NumberTickUnit(1));
        domainAxis.setNumberFormatOverride(new NumberFormat() {
            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                int index = (int) number;
                if (index >= 0 && index < datosGrafico.size()) {
                    return toAppendTo.append(datosGrafico.get(index).get("periodo"));
                }
                return toAppendTo;
            }

            @Override
            public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
                return format((double) number, toAppendTo, pos);
            }

            @Override
            public Number parse(String source, ParsePosition parsePosition) {
                return null;
            }
        });

        // Configurar el renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);

        // Agregar zonas de colores
        plot.addRangeMarker(createZonaMarker(0.0, 0.15, new Color(255, 182, 193)));
        plot.addRangeMarker(createZonaMarker(0.15, 0.30, new Color(255, 200, 124)));
        plot.addRangeMarker(createZonaMarker(0.30, 0.50, new Color(255, 235, 156)));
        plot.addRangeMarker(createZonaMarker(0.50, 1.0, new Color(169, 223, 191)));

        // Crear directorio y guardar
        File directorio = new File("reporteWord");
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        File outputFile = new File("reporteWord/Grafico_UT_escasez.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 1200, 400);
    }

    private org.jfree.chart.plot.IntervalMarker createZonaMarker(double start, double end, Color color) {
        org.jfree.chart.plot.IntervalMarker marker =
                new org.jfree.chart.plot.IntervalMarker(start, end);
        marker.setPaint(color);
        marker.setAlpha(0.5f);
        return marker;
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
