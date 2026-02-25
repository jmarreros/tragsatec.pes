package com.chc.pes.util;

import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTEscenarioProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.dto.general.EstacionPesUtProjection;
import com.chc.pes.dto.general.UnidadTerritorialProjection;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.poi.wp.usermodel.Paragraph;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSettings;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

// Clase utilitaria para operaciones relacionadas con documentos de Word
public class DocumentWordUtils {

    private static final String DESCONOCIDO = "desconocido";

    private static final String COLOR_FONDO_CABECERA = "5B9BD5";
    private static final String COLOR_FONDO_DATO = "D9E1F2";
    private static final String COLOR_BLANCO = "FFFFFF";
    private static final String COLOR_NEGRO = "000000";
    private static final String COLOR_BORDES_TABLA = "D0D0D0";

    public static XWPFTable crearTablaSinBordes(XWPFDocument documento, int filas, int columnas) {
        return crearTabla(documento, filas, columnas, false);
    }

    public static XWPFTable crearTablaConBordes(XWPFDocument documento, int filas, int columnas) {
        return crearTabla(documento, filas, columnas, true);
    }

    public static void aniadirBordesTabla(XWPFTable tabla) {
        CTTbl ctTbl = tabla.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr();
        if (tblPr == null) {
            tblPr = ctTbl.addNewTblPr();
        }

        CTTblBorders borders = tblPr.getTblBorders();
        if (borders == null) {
            borders = tblPr.addNewTblBorders();
        }

        setBorder(borders.addNewTop());
        setBorder(borders.addNewBottom());
        setBorder(borders.addNewLeft());
        setBorder(borders.addNewRight());
        setBorder(borders.addNewInsideH());
        setBorder(borders.addNewInsideV());
    }

    public static XWPFTable crearTabla(XWPFDocument documento, int filas, int columnas, boolean conBordes) {
        XWPFTable tabla = documento.createTable(filas, columnas);
        tabla.setWidth("100%");
        if (conBordes) {
            aniadirBordesTabla(tabla);
        }

        return tabla;
    }

    // Remplazos
    public static void encabezadoH2(XWPFDocument document, String nuevoTexto) {

        XWPFParagraph salto = document.getLastParagraph();
        salto.createRun().addBreak(BreakType.PAGE);

        XWPFParagraph h2 = document.createParagraph();
        h2.setStyle("Ttulo2");
        h2.createRun().setText(nuevoTexto);
        cambiarTextoParrafo(h2, nuevoTexto);

    }

    private static void cambiarTextoParrafo(XWPFParagraph paragraph, String nuevoTexto) {
        // Limpiar todos los runs existentes
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // Crear nuevo run con el texto actualizado
        XWPFRun run = paragraph.createRun();
        run.setText(nuevoTexto);
    }

    public static void cambiarMesYAnioEnParrafo(XWPFDocument document, Integer mes, Integer anio) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        String nombreMes = DateUtils.obtenerNombreMesCapitalizado(mes);
        for (XWPFParagraph paragraph : paragraphs) {
            DocumentWordUtils.reemplazarTextoEnParagrafo(paragraph, "<<mes>>", nombreMes);
            DocumentWordUtils.reemplazarTextoEnParagrafo(paragraph, "<<año>>", String.valueOf(anio));
        }
    }

    // Utilidades generales
    public static void reemplazarTextoEnParagrafo(XWPFParagraph paragraph, String buscar, String reemplazar) {
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

    public static List<Map<String, Object>> prepararDatosTotalesParaGrafico(List<IndicadorUTFechaDataProjection> datos) {

        if (datos == null || datos.isEmpty()) {
            return new ArrayList<>();
        }

        // Ordena los datos por año y mes usando Comparator
        return datos.stream().sorted(Comparator.comparingInt(IndicadorUTFechaDataProjection::getAnio).thenComparingInt(IndicadorUTFechaDataProjection::getMes)).map(item -> {
            // Usa DateFormatSymbols para obtener nombres de meses
            // String mesAbreviado = new DateFormatSymbols(new Locale("es",
            // "ES"))
            // .getShortMonths()[item.getMes() - 1];
            // String anioCorto = String.valueOf(item.getAnio()).substring(2);

            Map<String, Object> punto = new HashMap<>();
            punto.put("periodo", DateUtils.ObtenerPeriodo(item.getAnio(), item.getMes()));
            punto.put("valor", item.getIndicador());
            punto.put("mes", item.getMes());
            punto.put("anio", item.getAnio());

            return punto;
        }).collect(Collectors.toList());
    }

    private static void addLeyenda(XYSeriesCollection dataset, XYLineAndShapeRenderer renderer, Shape legendShape, Object... datos) {
        // datos = nombre1, color1, nombre2, color2, ...
        for (int i = 0; i < datos.length; i += 2) {
            String nombre = (String) datos[i];
            Color color = (Color) datos[i + 1];

            XYSeries serie = new XYSeries(nombre);
            dataset.addSeries(serie);

            int index = dataset.getSeriesCount() - 1;

            renderer.setSeriesPaint(index, color);
            renderer.setSeriesLinesVisible(index, false);
            renderer.setSeriesShapesVisible(index, true);
            renderer.setSeriesShapesFilled(index, true);
            renderer.setSeriesShape(index, legendShape);
        }
    }

    public static String generarGraficoLineas(char tipoGrafico, String temporalDir, String utCodigo, Integer anioHidrologico, List<Map<String, Object>> datosGrafico) throws IOException {

        String nombreGrafico = temporalDir + "/grafico_UT" + tipoGrafico + "_" + utCodigo + ".png";
        new File(nombreGrafico).delete();

        /*
         * ========================= 1. Construir año hidrológico completo
         * =========================
         */

        Map<String, Double> valoresPorPeriodo = datosGrafico.stream().collect(Collectors.toMap(d -> (String) d.get("periodo"), d -> ((Number) d.get("valor")).doubleValue()));

        List<String> periodos = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            int mes = (10 + i - 1) % 12 + 1;
            int anio = anioHidrologico + ((10 + i - 1) / 12);
            String periodo = DateUtils.ObtenerPeriodo(anio, mes);

            periodos.add(periodo);
            valores.add(valoresPorPeriodo.get(periodo));
        }

        /*
         * ========================= 2. Dataset =========================
         */

        XYSeries serie = new XYSeries("Indicador");
        for (int i = 0; i < valores.size(); i++) {
            if (valores.get(i) != null) {
                serie.add(i + 1, valores.get(i)); // X empieza en 1
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection(serie);

        /*
         * ========================= 3. Gráfico base =========================
         */

        JFreeChart chart = ChartFactory.createXYLineChart("", "Año hidrológico", "Valor del indicador", dataset, PlotOrientation.VERTICAL, true, false, false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        /*
         * ========================= 4. Eje Y =========================
         */

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 1.0);
        rangeAxis.setTickUnit(new NumberTickUnit(0.25));

        /*
         * ========================= 5. Eje X (etiquetas limpias)
         * =========================
         */

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setRange(0.5, valores.size() + 0.5);
        domainAxis.setTickUnit(new NumberTickUnit(1));
        domainAxis.setLowerMargin(0);
        domainAxis.setUpperMargin(0);

        domainAxis.setNumberFormatOverride(new NumberFormat() {
            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                int index = (int) number - 1;
                if (index >= 0 && index < periodos.size()) {
                    return toAppendTo.append(periodos.get(index));
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

        /*
         * ========================= 6. Renderer (puntos SIEMPRE visibles)
         * =========================
         */

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesStroke(0, new BasicStroke(2f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));

        plot.setRenderer(renderer);

        /*
         * ========================= 7. Zonas y leyenda
         * =========================
         */

        Shape legendShape = new Rectangle2D.Double(-5, -5, 10, 10);

        if (tipoGrafico == 'E') {
            chart.setTitle("Evolución del Indicador de Escasez de la Unidad Territorial " + utCodigo);

            plot.addRangeMarker(createZonaMarker(0.0, 0.15, new Color(255, 182, 193)));
            plot.addRangeMarker(createZonaMarker(0.15, 0.30, new Color(255, 200, 124)));
            plot.addRangeMarker(createZonaMarker(0.30, 0.50, new Color(255, 235, 156)));
            plot.addRangeMarker(createZonaMarker(0.50, 1.0, new Color(169, 223, 191)));

            addLeyenda(dataset, renderer, legendShape, "Normalidad", new Color(169, 223, 191), "Pre Alerta", new Color(255, 235, 156), "Alerta", new Color(255, 200, 124), "Emergencia", new Color(255, 182, 193));

        } else if (tipoGrafico == 'S') {
            chart.setTitle("Evolución del Indicador de Sequía de la Unidad Territorial " + utCodigo);

            plot.addRangeMarker(createZonaMarker(0.0, 0.30, new Color(255, 182, 193)));
            plot.addRangeMarker(createZonaMarker(0.30, 1.0, new Color(169, 223, 191)));

            addLeyenda(dataset, renderer, legendShape, "Normalidad", new Color(169, 223, 191), "Sequía prolongada", new Color(255, 182, 193));
        }
        Font fontActual = chart.getTitle().getFont();
        chart.getTitle().setFont(fontActual.deriveFont(Font.PLAIN));

        /*
         * ========================= 8. Guardar =========================
         */

        File dir = new File(temporalDir);
        if (!dir.exists())
            dir.mkdirs();

        ChartUtils.saveChartAsPNG(new File(nombreGrafico), chart, 1200, 400);
        return nombreGrafico;
    }

    private static org.jfree.chart.plot.IntervalMarker createZonaMarker(double start, double end, Color color) {
        org.jfree.chart.plot.IntervalMarker marker = new org.jfree.chart.plot.IntervalMarker(start, end);
        marker.setPaint(color);
        marker.setAlpha(0.5f);
        return marker;
    }

    public static void insertarGraficoUT(XWPFDocument document, String rutaGrafico) {
        // Verificar si el archivo existe
        if (!new File(rutaGrafico).exists()) {
            return;
        }

        try (InputStream is = new FileInputStream(rutaGrafico)) {
            BufferedImage img = ImageIO.read(new File(rutaGrafico));

            // Redimensionar manteniendo el aspect ratio
            int width = 500;
            int height = (int) (((double) img.getHeight() / img.getWidth()) * width);

            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = paragraph.createRun();

            run.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG, rutaGrafico, Units.toEMU(width), Units.toEMU(height));
        } catch (Exception e) {
            throw new RuntimeException("Error al insertar el gráfico UT: " + rutaGrafico, e);
        }
    }

    // Cambio de colores SVG en archivo y generación de PNG
    public static String procesarSVGFile(Character tipoReporte, // 'E' o 'S'
                                         // (Escasez o
                                         // Sequia),
                                         String reportDir, String temporalDir, List<IndicadorUTEscenarioProjection> listUTEscenario, String demarcacionCodigo) throws IOException {

        // Ruta de archivos
        String fileSvg = reportDir + "/svg/" + demarcacionCodigo + "_UT" + tipoReporte + ".svg";
        String outputPngPath = temporalDir + "/" + demarcacionCodigo + "_UT" + tipoReporte + ".png";

        // Eliminar el archivo existente
        Path outputPng = Path.of(outputPngPath);
        try {
            Files.deleteIfExists(outputPng);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Abrimos el archivo SVG y buscamos los códigos de las UTs para
        // actualizar sus colores
        String svg = Files.readString(Path.of(fileSvg), StandardCharsets.UTF_8);

        for (IndicadorUTEscenarioProjection item : listUTEscenario) {
            String buscar = "id=\"" + item.getCodigoDh() + "\" fill=\"#fff\"";
            String reemplazar = "id=\"" + item.getCodigoDh() + "\" fill=\"" + obtenerColorPorEscenarioUT(item.getEscenarioFinal()) + "\"";
            svg = svg.replace(buscar, reemplazar);
        }

        // Transcoding desde el SVG en memoria hacia PNG
        try (InputStream svgStream = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)); FileOutputStream pngOut = new FileOutputStream(outputPngPath)) {
            TranscoderInput input = new TranscoderInput(svgStream);
            TranscoderOutput output = new TranscoderOutput(pngOut);
            PNGTranscoder transcoder = new PNGTranscoder();

            transcoder.addTranscodingHint(PNGTranscoder.KEY_MEDIA, "screen");
            transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, 1600f);
            transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, 566f);
            transcoder.transcode(input, output);

            return outputPngPath;
        } catch (TranscoderException e) {
            throw new RuntimeException(e);
        }
    }

    private static String obtenerColorPorEscenarioUT(String escenario) {
        Escenario escenarioResult = Escenario.fromValue(escenario);
        return Escenario.getColor(escenarioResult);
    }

    public static void insertaImagenPrincipal(XWPFDocument document, String imgUTEs) throws IOException {
        // Insertamos el archivo .png generado al final del documento Word
        File imgFile = new File(imgUTEs);

        if (imgFile.exists()) {
            try (FileInputStream is = new FileInputStream(imgFile)) {

                BufferedImage img = ImageIO.read(imgFile);

                int widthPx = 650;
                int heightPx = (int) (((double) img.getHeight() / img.getWidth()) * widthPx);

                XWPFParagraph imgParagraph = document.createParagraph();
                imgParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun imgRun = imgParagraph.createRun();

                // Inserta la imagen (PNG). Units.pixelToEMU convierte píxeles a
                // EMU usados por Word.
                imgRun.addPicture(is, Document.PICTURE_TYPE_PNG, imgFile.getName(), Units.pixelToEMU(widthPx), Units.pixelToEMU(heightPx));

            } catch (Exception e) {
                throw new RuntimeException("Error al insertar la imagen en el documento Word: " + e.getMessage(), e);
            }
        } else {
            throw new FileNotFoundException("El archivo de imagen no se encontró: " + imgUTEs);
        }
    }

    public static void insertarLeyendaImagen(XWPFDocument document, String texto) {
        XWPFParagraph caption = document.createParagraph();
        caption.setAlignment(ParagraphAlignment.CENTER);
        caption.setSpacingAfter(200); // ajustar espacio después si hace falta
        caption.setSpacingBefore(200); // ajustar espacio antes si hace falta
        XWPFRun run = caption.createRun();
        run.setText(texto);
        run.setItalic(true);
        run.setBold(false);
        run.setFontSize(11.0); // ajustar tamaño según necesidad
        run.setColor("000000");
    }

    private static void addFormattedText(XWPFParagraph paragraph, String pattern, int fontSize, Object... args) {
        int argIndex = 0;
        int i = 0;

        while (i < pattern.length()) {
            int idx = pattern.indexOf("%s", i);

            if (idx == -1) {
                // Texto normal restante
                XWPFRun run = paragraph.createRun();
                run.setFontSize(fontSize);
                run.setText(pattern.substring(i));
                break;
            }

            // Texto normal antes del %s
            if (idx > i) {
                XWPFRun run = paragraph.createRun();
                run.setFontSize(fontSize);
                run.setText(pattern.substring(i, idx));
            }

            // Texto del %s en negrita
            XWPFRun boldRun = paragraph.createRun();
            boldRun.setBold(true);
            boldRun.setFontSize(fontSize);
            boldRun.setText(String.valueOf(args[argIndex++]));

            i = idx + 2; // saltar "%s"
        }
    }

    public static void insertarLeyendaTabla(XWPFDocument document, char tipoTabla, Integer anio, Integer mes, Double valor, String escenario) {
        String ut = (tipoTabla == 'E') ? "UTE" : "UTS";


        String nombreMes = (mes != null) ? DateUtils.obtenerNombreMesCapitalizado(mes) : "-";
        String anioStr = (anio != null) ? String.valueOf(anio) : "-";
        DecimalFormat df = new DecimalFormat("0.000");
        String escenarioStr = (escenario != null && !escenario.isBlank()) ? escenario.toUpperCase(new Locale("es", "ES")) : "-";
        if (escenarioStr.equalsIgnoreCase("EMERGENCIA") && tipoTabla == 'S')
            escenarioStr = "SEQUÍA PROLONGADA";

        XWPFParagraph caption = document.createParagraph();
        caption.setAlignment(ParagraphAlignment.CENTER);
        caption.setSpacingAfter(200);

        int fontSize = 11;

        if (valor != null) {

            addFormattedText(caption, "En el mes de %s de %s, el indicador %s alcanza un valor de %s (ver tabla y gráfico).", fontSize, nombreMes, anioStr, ut, df.format(valor));

            caption.createRun().addBreak();

            addFormattedText(caption, "La %s se encuentra en escenario de %s (ver imagen superior).", fontSize, ut, escenarioStr);

        } else {

            addFormattedText(caption, "En el mes de %s de %s, no existen datos para calcular el indicador de la %s.", fontSize, nombreMes, anioStr, ut);
        }
    }

    public static void insertarComentarioUt(XWPFDocument document, String comentario) {
        if (comentario == null || comentario.trim().isEmpty()) {
            return;
        }

        XWPFParagraph pComentario = document.createParagraph();
        pComentario.setAlignment(ParagraphAlignment.LEFT);
        pComentario.setSpacingAfter(200);
        pComentario.setSpacingBefore(200);
        XWPFRun runComentario = pComentario.createRun();
        runComentario.setFontSize(7.0);
        runComentario.setColor("000000");

        // Respetar saltos de línea en el comentario
        String[] lines = comentario.split("\\r?\\n");
        if (lines.length > 0) {
            runComentario.setText(lines[0], 0);
            for (int i = 1; i < lines.length; i++) {
                runComentario.addBreak();
                runComentario.setText(lines[i]);
            }
        } else {
            runComentario.setText(comentario, 0);
        }
    }

    // Creación de tabla principal
    public static void crearTablaUT(XWPFDocument document, List<IndicadorDemarcacionFechaDataProjection> datos, Integer anioHidrologico) {
        if (datos == null || datos.isEmpty()) {
            return;
        }

        // Almacenar el objeto completo
        java.util.Map<String, java.util.Map<String, IndicadorDemarcacionFechaDataProjection>> datosPorUT = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> ordenMeses = new java.util.HashMap<>();

        for (int i = 10; i <= 12; i++) {
            String label = DateUtils.ObtenerPeriodo(anioHidrologico, i);
            int orden = anioHidrologico * 100 + i;
            ordenMeses.putIfAbsent(label, orden);
        }
        for (int i = 1; i <= 9; i++) {
            String label = DateUtils.ObtenerPeriodo(anioHidrologico + 1, i);
            int orden = (anioHidrologico + 1) * 100 + i;
            ordenMeses.putIfAbsent(label, orden);
        }
        for (IndicadorDemarcacionFechaDataProjection d : datos) {
            String ut = d.getUtNombre();
            String label = DateUtils.ObtenerPeriodo(d.getAnio(), d.getMes());

            datosPorUT.computeIfAbsent(ut, k -> new java.util.HashMap<>()).put(label, d);
        }

        java.util.List<String> mesesOrdenados = ordenMeses.entrySet().stream().sorted(java.util.Map.Entry.comparingByValue()).map(java.util.Map.Entry::getKey).toList();

        int filas = datosPorUT.size() + 1;

        XWPFTable table = crearTablaConBordes(document, filas, 13);

        // Cabecera
        XWPFTableRow headerRow = table.getRow(0);
        configurarAlturaFila(headerRow); // 20px ≈ 300 twips
        configurarCeldaCabecera(headerRow.getCell(0), "Unidades Territoriales");

        for (int c = 0; c < mesesOrdenados.size(); c++) {
            XWPFTableCell cell = headerRow.getCell(c + 1);
            configurarCeldaCabecera(cell, mesesOrdenados.get(c));
        }

        java.text.DecimalFormat df = new java.text.DecimalFormat("0.000");

        // Filas por UT
        int filaIdx = 1;
        for (java.util.Map.Entry<String, java.util.Map<String, IndicadorDemarcacionFechaDataProjection>> entry : datosPorUT.entrySet()) {
            String ut = entry.getKey();
            java.util.Map<String, IndicadorDemarcacionFechaDataProjection> valores = entry.getValue();

            XWPFTableRow row = table.getRow(filaIdx);
            configurarAlturaFila(row); // 20px ≈ 300 twips

            // Primera celda: nombre UT
            XWPFTableCell cellUT = row.getCell(0);
            configurarAlineacionVertical(cellUT);
            cellUT.removeParagraph(0);
            XWPFParagraph pUT = cellUT.addParagraph();
            pUT.setAlignment(ParagraphAlignment.LEFT);
            eliminarEspaciadoParrafo(pUT);
            XWPFRun runUT = pUT.createRun();
            runUT.setBold(false);
            runUT.setFontSize(7.0);
            runUT.setText(ut);

            // Valores por mes
            for (int c = 0; c < mesesOrdenados.size(); c++) {
                String mesLabel = mesesOrdenados.get(c);
                XWPFTableCell cell = row.getCell(c + 1);
                configurarAlineacionVertical(cell);

                cell.removeParagraph(0);
                XWPFParagraph p = cell.addParagraph();
                p.setAlignment(ParagraphAlignment.CENTER);
                eliminarEspaciadoParrafo(p);
                XWPFRun run = p.createRun();
                run.setFontSize(8.0);

                IndicadorDemarcacionFechaDataProjection dato = valores.get(mesLabel);
                if (dato == null || dato.getIndicador() == null) {
                    run.setText("-");
                } else {
                    run.setText(df.format(dato.getIndicador()));
                    // Pintar celda según el escenario
                    if (dato.getEscenarioFinal() != null && !dato.getEscenarioFinal().isBlank()) {
                        String colorHex = obtenerColorPorEscenarioUT(dato.getEscenarioFinal());
                        cell.setColor(colorHex.substring(1)); // Quitar el '#'
                    }
                }
            }
            filaIdx++;
        }
    }

    private static void configurarAlturaFila(XWPFTableRow row) {
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr trPr = row.getCtRow().isSetTrPr() ? row.getCtRow().getTrPr() : row.getCtRow().addNewTrPr();

        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight height = trPr.sizeOfTrHeightArray() > 0 ? trPr.getTrHeightArray(0) : trPr.addNewTrHeight();

        height.setVal(java.math.BigInteger.valueOf(300));
        height.setHRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule.AT_LEAST);
    }

    private static void eliminarEspaciadoParrafo(XWPFParagraph p) {
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);
        p.setSpacingBetween(1.0);
    }

    public static void configurarCeldaCabecera(XWPFTableCell cell, String texto) {
        // Verificar si la celda no tiene color entonces asignar color por
        // defecto
        if (cell.getColor() == null || cell.getColor().isEmpty()) {
            cell.setColor(COLOR_FONDO_CABECERA);
            setMargin(cell, 100);
        }

        configurarAlineacionVertical(cell);

        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        eliminarEspaciadoParrafo(p);

        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setFontSize(7.0);
        run.setColor(COLOR_BLANCO);

        // Manejar saltos de línea manuales
        if (texto.contains("\n")) {
            String[] lines = texto.split("\n");
            run.setText(lines[0], 0);
            for (int i = 1; i < lines.length; i++) {
                run.addBreak();
                run.setText(lines[i]);
            }
        } else {
            run.setText(texto);
        }
    }

    private static void establecerCeldaEstaciones(XWPFTableCell cell, String texto, int spacing, String colorTexto, String colorFondo) {
        // Verificar si la celda no tiene color entonces asignar color por
        // defecto
        if (cell.getColor() == null || cell.getColor().isEmpty()) {
            cell.setColor(colorFondo);
        }

        CTTc ctTc = cell.getCTTc();
        CTTcPr tcPr = ctTc.getTcPr();
        CTTcBorders border = tcPr.addNewTcBorders();

        setBorder(border.addNewTop());
        setBorder(border.addNewBottom());
        setBorder(border.addNewLeft());
        setBorder(border.addNewRight());
        setBorder(border.addNewInsideH());
        setBorder(border.addNewInsideV());

        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setSpacingBefore(spacing);
        p.setSpacingAfter(spacing);
        p.setAlignment(ParagraphAlignment.CENTER);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

        XWPFRun run = p.createRun();
        if (spacing > 100)
            run.setBold(true);
        run.setFontSize(7.0);
        run.setColor(colorTexto);
        // Manejar saltos de línea manuales
        if (texto.contains("\n")) {
            String[] lines = texto.split("\n");
            run.setText(lines[0], 0);
            for (int i = 1; i < lines.length; i++) {
                run.addBreak();
                run.setText(lines[i]);
            }
        } else {
            run.setText(texto);
        }
    }

    private static void configurarAlineacionVertical(XWPFTableCell cell) {
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();

        if (!tcPr.isSetVAlign()) {
            tcPr.addNewVAlign();
        }
        tcPr.getVAlign().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc.CENTER);
    }

    public static void tablaUnidadTerritorialDetalladaa(XWPFTable tabla) {
        tabla.setWidth("100%");
        // 1. Eliminar bordes (hacer la tabla invisible)
        tabla.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, null);
        tabla.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, null);
        tabla.getCTTbl().getTblPr().getTblBorders().getTop().setVal(STBorder.NONE);
        tabla.getCTTbl().getTblPr().getTblBorders().getBottom().setVal(STBorder.NONE);
        tabla.getCTTbl().getTblPr().getTblBorders().getLeft().setVal(STBorder.NONE);
        tabla.getCTTbl().getTblPr().getTblBorders().getRight().setVal(STBorder.NONE);
        List<XWPFTableRow> rows = tabla.getRows();
        int totalFilas = rows.size();

        for (int i = 0; i < totalFilas; i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> celdas = row.getTableCells();

            for (int j = 0; j < celdas.size(); j++) {
                XWPFTableCell cell = celdas.get(j);

                // 1. Configurar anchos (siempre necesario para estabilidad)
                configurarAnchoPorcentaje(cell, j);

                // --- REGLA 1: Fila 1 (Índice 0) -> Columnas 1, 2 y 3 se UNEN
                // ---
                if (i == 0 && j < 3) {
                    CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
                    CTHMerge hMerge = tcPr.isSetHMerge() ? tcPr.getHMerge() : tcPr.addNewHMerge();

                    if (j == 0) {
                        hMerge.setVal(STMerge.RESTART); // La que "sobrevive"
                    } else {
                        hMerge.setVal(STMerge.CONTINUE); // Las que se
                        // "fusionan"
                        limpiarCelda(cell);
                    }
                }

                // --- REGLA 2: Columna 4 (Índice 3) -> Desde Fila 2 (Índice 1)
                // hasta el final se UNEN ---
                if (j == 3 && i >= 1) {
                    CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
                    CTVMerge vMerge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();

                    if (i == 1) {
                        vMerge.setVal(STMerge.RESTART); // La que "sobrevive"
                        // (en la segunda fila)
                    } else {
                        vMerge.setVal(STMerge.CONTINUE); // El resto de filas
                        // hacia abajo
                        limpiarCelda(cell);
                    }
                }
            }
        }
    }

    // Método auxiliar vital para evitar el error de "Contenido Ilegible"
    private static void limpiarCelda(XWPFTableCell cell) {
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }
        cell.addParagraph(); // Word exige mínimo un párrafo aunque esté vacío
    }

    private static void configurarAnchoPorcentaje(XWPFTableCell cell, int colIndex) {
        int pct = switch (colIndex) {
            case 0 -> 20;
            case 1 -> 20;
            case 2 -> 10;
            case 3 -> 50;
            default -> 0;
        };
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTblWidth tcW = tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
        tcW.setType(STTblWidth.PCT);
        tcW.setW(BigInteger.valueOf(pct * 50));
    }

    public static void crearContenidoSimple1x2(XWPFDocument document, List<EstacionPesUtProjection> estaciones, String imagePath, String tituloImagen, int orden) throws Exception {

        // 1. Crear tabla vacía (sin fusiones aún)
        XWPFTable contenedorNuevo = crearTablaSinBordes(document, estaciones.size() + 3, 4);

        // --- FILA 0: Títulos ---
        XWPFTableCell celdaE = contenedorNuevo.getRow(0).getCell(0);
        cabeceraContenidoSimple1x2(celdaE, "ESTACIONES SELECCIONADAS Y PONDERACIÓN", ParagraphAlignment.CENTER);

        XWPFTableCell celdaT = contenedorNuevo.getRow(0).getCell(3);
        cabeceraContenidoSimple1x2(celdaT, tituloImagen, ParagraphAlignment.RIGHT);

        // --- FILA 1: Cabeceras ---
        establecerCeldaEstaciones(contenedorNuevo.getRow(1).getCell(0), "Nombre y código", 150, COLOR_BLANCO, COLOR_FONDO_CABECERA);
        establecerCeldaEstaciones(contenedorNuevo.getRow(1).getCell(1), "Ponderación (%)", 150, COLOR_BLANCO, COLOR_FONDO_CABECERA);
        establecerCeldaEstaciones(contenedorNuevo.getRow(1).getCell(2), "Coordenadas", 150, COLOR_BLANCO, COLOR_FONDO_CABECERA);

        // --- FILA 1, COLUMNA 4: Imagen ---
        // --- 3. GESTIÓN DE LA IMAGEN CON NOMBRE ÚNICO ---
        XWPFTableCell celdaImagen = contenedorNuevo.getRow(1).getCell(3);
        celdaImagen.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.TOP);
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                // 1. Obtener dimensiones (Nervión, Oria, etc.)
                BufferedImage img = ImageIO.read(file);
                int width = 250;
                int height = (int) (((double) img.getHeight() / img.getWidth()) * width);

                XWPFParagraph paragraph = celdaImagen.getParagraphs().get(0);
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun run = paragraph.createRun();

                try (InputStream is = new FileInputStream(file)) {
                    run.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG, "image.png", Units.toEMU(width), Units.toEMU(height));

                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Error en UTS " + orden + ": " + imagePath, e);
        }

        DecimalFormat df = new DecimalFormat("#,##0.00");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("es", "ES")));

        // Ordenar estaciones por nombre
        estaciones.sort((e1, e2) -> e1.getNombre().compareToIgnoreCase(e2.getNombre()));

        // --- FILAS 2+ : Datos ---
        for (int k = 0; k < estaciones.size(); k++) {
            XWPFTableRow fila = contenedorNuevo.getRow(k + 2);
            EstacionPesUtProjection e = estaciones.get(k);

            establecerCeldaEstaciones(fila.getCell(0), e.getNombre() + "\n(" + e.getCodigo() + ")", 0, COLOR_NEGRO, colorFondo(k));
            establecerCeldaEstaciones(fila.getCell(1), df.format(e.getCoeficiente()), 0, COLOR_NEGRO, colorFondo(k));
            establecerCeldaEstaciones(fila.getCell(2), formatearCoordenadas(e.getCoordenadas()), 0, COLOR_NEGRO, colorFondo(k));

            // IMPORTANTE: Aseguramos que la celda de la columna 4 esté vacía
            // antes de fusionar
            limpiarCeldaEsclava(fila.getCell(3));
        }

        // --- 4. APLICAR FUSIONES AL FINAL ---
        tablaUnidadTerritorialDetalladaa(contenedorNuevo);

    }

    private static void cabeceraContenidoSimple1x2(XWPFTableCell celda, String texto, ParagraphAlignment alineacion) {
        XWPFParagraph p = celda.getParagraphs().get(0);
        p.setAlignment(alineacion);
        p.setSpacingBefore(200);
        p.setSpacingAfter(100);
        XWPFRun r = p.createRun();
        r.setText(texto);
        r.setBold(true);
        r.setFontSize(10);
    }

    private static void limpiarCeldaEsclava(XWPFTableCell celda) {
        // Elimina cualquier párrafo que Apache POI cree por defecto
        while (celda.getParagraphs().size() > 0) {
            celda.removeParagraph(0);
        }
        // Añade uno solo, totalmente vacío
        celda.addParagraph();
    }

    private static CTTcPr getOrCreateTcPr(XWPFTableCell cell) {
        CTTc ctTc = cell.getCTTc();
        return ctTc.isSetTcPr() ? ctTc.getTcPr() : ctTc.addNewTcPr();
    }

    private static void setColorFondo(XWPFTableCell cell, String hexColor) {
        CTTcPr tcPr = getOrCreateTcPr(cell);
        CTShd shd = tcPr.addNewShd();
        shd.setFill(hexColor);
        CTTcMar mar = tcPr.addNewTcMar();
        mar.addNewTop().setW(10);
    }

    private static void setMargin(XWPFTableCell cell, int size) {
        CTTcPr tcPr = getOrCreateTcPr(cell);
        CTTcMar mar = tcPr.addNewTcMar();
        mar.addNewTop().setW(size);
        mar.addNewBottom().setW(size);
    }

    private static String colorFondo(int i) {
        return (i % 2 == 0) ? COLOR_BLANCO : COLOR_FONDO_DATO;
    }

    private static void setBorder(CTBorder border) {
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(8));
        border.setColor(COLOR_BORDES_TABLA);
    }

    private static String formatearCoordenadas(String coordenadas) {
        if (coordenadas == null || coordenadas.trim().isEmpty())
            return "-";
        String[] partes = coordenadas.split("\\s*,\\s*");
        return partes.length == 2 ? "x = " + partes[0].trim() + "\ny = " + partes[1].trim() : coordenadas;
    }

    private static void configurarCeldaDato(XWPFTableCell cell, String texto, ParagraphAlignment alignment, String colorFondo) {
        configurarAlineacionVertical(cell);
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(alignment);
        eliminarEspaciadoParrafo(p);
        XWPFRun run = p.createRun();
        run.setFontSize(7.0);
        setColorFondo(cell, colorFondo);

        // Manejar saltos de línea manuales
        if (texto.contains("\n")) {
            String[] lines = texto.split("\n");
            run.setText(lines[0], 0);
            for (int i = 1; i < lines.length; i++) {
                run.addBreak();
                run.setText(lines[i]);
            }
        } else {
            run.setText(texto);
        }
    }

    // Tabla de datos por estación y totales Por Unidad Territorial
    public static void crearTablaDatosEstacionesUT(XWPFDocument document, List<IndicadorUTFechaDataProjection> datosEstaciones, List<IndicadorUTFechaDataProjection> datosTotales, String nombreUT) {
        // Si no hay datos, no se puede determinar el año hidrológico, no hacer
        // nada.
        if ((datosEstaciones == null || datosEstaciones.isEmpty()) && (datosTotales == null || datosTotales.isEmpty())) {
            return;
        }

        // 1. Determinar el año hidrológico a partir de los datos
        IndicadorUTFechaDataProjection primerDato = (datosEstaciones != null && !datosEstaciones.isEmpty()) ? datosEstaciones.get(0) : datosTotales.get(0);

        int anioDato = primerDato.getAnio();
        int mesDato = primerDato.getMes();
        int anioHidrologico = (mesDato < 10) ? anioDato - 1 : anioDato;

        // 2. Preparar cabeceras para el año hidrológico completo
        Map<String, Integer> ordenMeses = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            int mes = (10 + i - 1) % 12 + 1; // Oct=10, Nov=11, ..., Sep=9
            int anio = anioHidrologico + ((10 + i - 1) / 12);
            String label = DateUtils.ObtenerPeriodo(anio, mes);
            int orden = anio * 100 + mes;
            ordenMeses.put(label, orden);
        }

        List<String> mesesOrdenados = new ArrayList<>(ordenMeses.keySet());

        // 3. Agrupar datos por estación
        Map<String, List<IndicadorUTFechaDataProjection>> datosPorEstacion = (datosEstaciones != null) ? datosEstaciones.stream().sorted(Comparator.comparing(IndicadorUTFechaDataProjection::getNombre)) // 1.
                // Ordenamos
                // por
                // nombre
                .collect(Collectors.groupingBy(IndicadorUTFechaDataProjection::getNombre, LinkedHashMap::new, // 2.
                        // Mantenemos
                        // el
                        // orden
                        // de
                        // inserción
                        Collectors.toList()))
                : new LinkedHashMap<>();
        // 4. Crear la tabla
        int filas = 1 + (datosPorEstacion.size() * 2) + 1; // Cabecera + 2 por
        // cada estación +
        // total
        int cols = 2 + mesesOrdenados.size(); // Nombre, Tipo + periodos
        XWPFTable table = crearTablaConBordes(document, filas, cols);

        // 5. Llenar cabecera
        XWPFTableRow headerRow = table.getRow(0);

        configurarCeldaCabecera(headerRow.getCell(0), "Nombre");
        configurarCeldaCabecera(headerRow.getCell(1), "Tipo");
        for (int i = 0; i < mesesOrdenados.size(); i++) {
            String cabeceraPeriodo = mesesOrdenados.get(i).replace("-", "\n");
            configurarCeldaCabecera(headerRow.getCell(i + 2), cabeceraPeriodo);
        }

        // 6. Llenar filas de datos
        int filaIdx = 1;
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.000");
        int idFila = 0;
        for (Map.Entry<String, List<IndicadorUTFechaDataProjection>> entry : datosPorEstacion.entrySet()) {
            String nombreEstacion = entry.getKey();
            String colorFondo = colorFondo(idFila);

            List<IndicadorUTFechaDataProjection> datosDeLaEstacion = entry.getValue();
            Map<String, IndicadorUTFechaDataProjection> valoresPorPeriodo = datosDeLaEstacion.stream().collect(Collectors.toMap(d -> DateUtils.ObtenerPeriodo(d.getAnio(), d.getMes()), d -> d));

            // La fila de Valor, luego la de Indicador
            XWPFTableRow filaValor = table.getRow(filaIdx);
            XWPFTableRow filaIndicador = table.getRow(filaIdx + 1);

            // Celda de Nombre con fusión vertical
            XWPFTableCell celdaNombre = filaValor.getCell(0);
            configurarCeldaDato(celdaNombre, nombreEstacion, ParagraphAlignment.LEFT, colorFondo);
            celdaNombre.getCTTc().addNewTcPr().addNewVMerge().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.RESTART);
            XWPFTableCell celdaNombreContinuacion = filaIndicador.getCell(0);
            celdaNombreContinuacion.getCTTc().addNewTcPr().addNewVMerge().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.CONTINUE);

            // Celdas de Tipo (usar unidad de medida para la fila de valor)
            String unidadMedidaStr = "Valor"; // Valor por defecto
            if (!datosDeLaEstacion.isEmpty()) {
                String unidad = datosDeLaEstacion.get(0).getUnidadMedida();
                if (unidad != null && !unidad.isBlank()) {
                    unidadMedidaStr = unidad;
                }
            }
            // formatear la celda de unidad de medida
            configurarCeldaUnidadMedida(filaValor.getCell(1), unidadMedidaStr, colorFondo);
            configurarCeldaDato(filaIndicador.getCell(1), "Indicador", ParagraphAlignment.CENTER, colorFondo);

            // Celdas de datos por período
            for (int i = 0; i < mesesOrdenados.size(); i++) {
                String periodo = mesesOrdenados.get(i).replace("\n", "-"); // Revertir
                // para
                // búsqueda
                IndicadorUTFechaDataProjection dato = valoresPorPeriodo.get(periodo);
                String indicadorStr = "-";
                String valorStr = "-";
                if (dato != null) {
                    if (dato.getIndicador() != null)
                        indicadorStr = df.format(dato.getIndicador());
                    if (dato.getValor() != null)
                        valorStr = df.format(dato.getValor());
                }
                configurarCeldaDato(filaValor.getCell(i + 2), valorStr, ParagraphAlignment.CENTER, colorFondo);
                configurarCeldaDato(filaIndicador.getCell(i + 2), indicadorStr, ParagraphAlignment.CENTER, colorFondo);
            }
            filaIdx += 2;
            idFila++;
        }

        // 7. Fila de totales
        XWPFTableRow totalRow = table.getRow(filaIdx);
        configurarAlturaFila(totalRow);

        Map<String, IndicadorUTFechaDataProjection> totalesPorPeriodo = (datosTotales != null) ? datosTotales.stream().collect(Collectors.toMap(d -> DateUtils.ObtenerPeriodo(d.getAnio(), d.getMes()), d -> d, (d1, d2) -> d1)) : new HashMap<>();

        configurarCeldaCabecera(totalRow.getCell(0), "UT - " + nombreUT);
        configurarCeldaCabecera(totalRow.getCell(1), "Indicador");

        for (int i = 0; i < mesesOrdenados.size(); i++) {
            String periodo = mesesOrdenados.get(i).replace("\n", "-");
            IndicadorUTFechaDataProjection datoTotal = totalesPorPeriodo.get(periodo);
            XWPFTableCell cell = totalRow.getCell(i + 2);

            String totalStr = "-";
            if (datoTotal != null && datoTotal.getIndicador() != null) {
                totalStr = df.format(datoTotal.getIndicador());
                if (datoTotal.getEscenarioFinal() != null && !datoTotal.getEscenarioFinal().isBlank()) {
                    String colorHex = obtenerColorPorEscenarioUT(datoTotal.getEscenarioFinal());
                    cell.setColor(colorHex.substring(1)); // Quitar el '#'
                }
            }
            configurarCeldaCabecera(cell, totalStr);
        }
    }

    private static void configurarCeldaUnidadMedida(XWPFTableCell cell, String texto, String colorFondo) {
        configurarAlineacionVertical(cell);
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        eliminarEspaciadoParrafo(p);

        String partePrincipal = texto;
        String partePequena = "";

        int parentesisIndex = texto.indexOf('(');
        if (parentesisIndex != -1) {
            partePrincipal = texto.substring(0, parentesisIndex).trim();
            partePequena = texto.substring(parentesisIndex).trim();
        }

        // Run para la parte principal
        XWPFRun runPrincipal = p.createRun();
        runPrincipal.setFontSize(8.0);

        // Reemplazar el primer espacio en la parte principal por un salto de
        // línea
        if (partePrincipal.contains(" ")) {
            String[] partes = partePrincipal.split(" ", 2);
            runPrincipal.setText(partes[0]);
            runPrincipal.addBreak();
            runPrincipal.setText(partes[1]);
        } else {
            runPrincipal.setText(partePrincipal);
        }

        // Si hay parte pequeña, añadirla con su propio formato en una nueva
        // línea
        if (!partePequena.isEmpty()) {
            runPrincipal.addBreak();

            XWPFRun runPequena = p.createRun();
            runPequena.setFontSize(5.5);
            runPequena.setText(partePequena);
        }

        setColorFondo(cell, colorFondo);
    }

    // Auxiliares para obtener datos actuales
    public static String getCurrentUTEscenario(Integer utId, List<IndicadorUTEscenarioProjection> listUTEscenario) {
        if (utId == null || listUTEscenario == null || listUTEscenario.isEmpty())
            return DESCONOCIDO;

        return listUTEscenario.stream().filter(e -> Long.valueOf(utId).equals(e.getId())).map(IndicadorUTEscenarioProjection::getEscenarioFinal).findFirst().orElse(DESCONOCIDO);
    }


    public static Double getCurrentUTIndicadorTotalMes(Integer utId, Integer mes, List<IndicadorUTFechaDataProjection> totalesUTFecha) {

        if (utId == null || mes == null || totalesUTFecha == null || totalesUTFecha.isEmpty()) {
            return null;
        }

        return totalesUTFecha.stream().filter(e -> utId.equals(e.getId()) && mes.equals(e.getMes())).map(IndicadorUTFechaDataProjection::getIndicador).findFirst().orElse(null);
    }

    public static String nombreImagenUTActual(String reportDir, char tipoReporte, List<IndicadorUTEscenarioProjection> listUTEscenario, UnidadTerritorialProjection ut) {

        String escenarioUt = getCurrentUTEscenario(ut.getId(), listUTEscenario);

        return reportDir + "/png/ut" + Character.toLowerCase(tipoReporte) + "/" + ut.getCodigoDh() + "-" + escenarioUt + ".png";
    }

    public static void crearDirectorioSiNoExiste(String temporalDirectory) {
        File directory = new File(temporalDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static void finalizarDocumento(XWPFDocument document) {
        // 0 . Asegurar que los campos estén actualizados
        //document.enforceUpdateFields();

    }

}
