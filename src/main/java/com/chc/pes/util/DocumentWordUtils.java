package com.chc.pes.util;

import com.chc.pes.dto.calculo.IndicadorUTEscenarioProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// Clase utilitaria para operaciones relacionadas con documentos de Word
public class DocumentWordUtils {

    // Remplazos
    public static void encabezadoH2(XWPFDocument document, String nuevoTexto) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        XWPFParagraph encabezadoOriginal = null;

        for (XWPFParagraph paragraph : paragraphs) {
            String texto = paragraph.getText();

            // Buscar párrafo con estilo específico
            if (paragraph.getStyle() != null &&
                    (paragraph.getStyle().equals("Heading2") || texto.contains("INTRODUCCIÓN"))) {
                encabezadoOriginal = paragraph;
                break;
            }
        }

        if (encabezadoOriginal != null) {
            XWPFParagraph nuevoEncabezado = copiarParrafoConEstilos(document, encabezadoOriginal);
            cambiarTextoParrafo(nuevoEncabezado, nuevoTexto);
        }

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
    private static XWPFParagraph copiarParrafoConEstilos(XWPFDocument document, XWPFParagraph original) {
        XWPFParagraph nuevoParrafo = document.createParagraph();

        // Copiar estilo
        if (original.getStyle() != null) {
            nuevoParrafo.setStyle(original.getStyle());
        }

        // Copiar alineación
        nuevoParrafo.setAlignment(original.getAlignment());

        // Copiar espaciado
        if (original.getCTP().getPPr() != null) {
            if (nuevoParrafo.getCTP().getPPr() == null) {
                nuevoParrafo.getCTP().addNewPPr();
            }

            CTPPr pprOriginal = original.getCTP().getPPr();
            CTPPr pprNuevo = nuevoParrafo.getCTP().getPPr();

            if (pprOriginal.isSetSpacing()) {
                pprNuevo.setSpacing(pprOriginal.getSpacing());
            }
            if (pprOriginal.isSetInd()) {
                pprNuevo.setInd(pprOriginal.getInd());
            }
        }

        // Copiar runs con su formato
        for (XWPFRun runOriginal : original.getRuns()) {
            XWPFRun nuevoRun = nuevoParrafo.createRun();

            // Copiar texto
            nuevoRun.setText(runOriginal.getText(0));

            // Copiar formato
            nuevoRun.setBold(runOriginal.isBold());
            nuevoRun.setItalic(runOriginal.isItalic());
            nuevoRun.setFontFamily(runOriginal.getFontFamily());

            Double fontSize = runOriginal.getFontSizeAsDouble();
            if (fontSize != null) {
                nuevoRun.setFontSize(fontSize);
            }

            if (runOriginal.getColor() != null) {
                nuevoRun.setColor(runOriginal.getColor());
            }
        }

        return nuevoParrafo;
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
    public static void configurarOrientacionHorizontal(XWPFParagraph paragraph) {
        CTPPr ppr = paragraph.getCTP().getPPr();
        if (ppr == null) {
            ppr = paragraph.getCTP().addNewPPr();
        }

        CTSectPr sectPr = ppr.getSectPr();
        if (sectPr == null) {
            sectPr = ppr.addNewSectPr();
        }

        CTPageSz pageSize = sectPr.getPgSz();
        if (pageSize == null) {
            pageSize = sectPr.addNewPgSz();
        }

        // Establecer orientación horizontal (landscape)
        pageSize.setOrient(STPageOrientation.LANDSCAPE);

        // Intercambiar ancho y alto para orientación horizontal
        // Tamaño carta: 11906 x 15840 (ancho x alto en vertical)
        pageSize.setW(BigInteger.valueOf(15840)); // Ancho en horizontal
        pageSize.setH(BigInteger.valueOf(11906)); // Alto en horizontal
    }
    public static void agregarSaltoDePagina(XWPFDocument document) {
        XWPFParagraph saltoPagina = document.createParagraph();
        XWPFRun runSalto = saltoPagina.createRun();
        runSalto.addBreak(BreakType.PAGE);
    }


    public static List<Map<String, Object>> prepararDatosTotalesParaGrafico(
            List<IndicadorUTFechaDataProjection> datos) {

        if (datos == null || datos.isEmpty()) {
            return new ArrayList<>();
        }

        // Ordena los datos por año y mes usando Comparator
        return datos.stream()
                .sorted(Comparator.comparingInt(IndicadorUTFechaDataProjection::getAnio)
                        .thenComparingInt(IndicadorUTFechaDataProjection::getMes))
                .map(item -> {
                    // Usa DateFormatSymbols para obtener nombres de meses
                    String mesAbreviado = new DateFormatSymbols(new Locale("es", "ES"))
                            .getShortMonths()[item.getMes() - 1];
                    String anioCorto = String.valueOf(item.getAnio()).substring(2);

                    Map<String, Object> punto = new HashMap<>();
                    punto.put("periodo", mesAbreviado + "-" + anioCorto);
                    punto.put("valor", item.getIndicador());
                    punto.put("mes", item.getMes());
                    punto.put("anio", item.getAnio());

                    return punto;
                })
                .collect(Collectors.toList());
    }

    // Generación de archivo de gráfico
    public static void generarGraficoLineas(List<Map<String, Object>> datosGrafico) throws IOException {
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
    private static org.jfree.chart.plot.IntervalMarker createZonaMarker(double start, double end, Color color) {
        org.jfree.chart.plot.IntervalMarker marker =
                new org.jfree.chart.plot.IntervalMarker(start, end);
        marker.setPaint(color);
        marker.setAlpha(0.5f);
        return marker;
    }


    // Cambio de colores SVG en archivo y generación de PNG
    public static String procesarSVGFile(
            Character tipoReporte, //'E' o 'S' (Escasez o Sequia),
            String reportDir,
            List<IndicadorUTEscenarioProjection> listUTEscenario,
            String demarcacionCodigo) throws IOException {

        // Ruta de archivos
        String pathFile = reportDir + "/plantillas/" + demarcacionCodigo + "_UT" + tipoReporte;
        String fileSvg = pathFile + ".svg";
        String outputPngPath = pathFile + ".png";

        // Eliminar el archivo existente
        Path outputPng = Path.of(outputPngPath);
        try {
            Files.deleteIfExists(outputPng);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Abrimos el archivo SVG y buscamos los códigos de las UTs para actualizar sus colores
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
                int widthPx = 800;
                int heightPx = 283;

                XWPFParagraph imgParagraph = document.createParagraph();
                imgParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun imgRun = imgParagraph.createRun();

                // Inserta la imagen (PNG). Units.pixelToEMU convierte píxeles a EMU usados por Word.
                imgRun.addPicture(is, Document.PICTURE_TYPE_PNG, imgFile.getName(),
                        Units.pixelToEMU(widthPx), Units.pixelToEMU(heightPx));

            } catch (Exception e) {
                throw new RuntimeException("Error al insertar la imagen en el documento Word: " + e.getMessage(), e);
            }
        } else {
            throw new FileNotFoundException("El archivo de imagen no se encontró: " + imgUTEs);
        }
    }
}

