package com.chc.pes.service.reporte;

import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTEscenarioProjection;
import com.chc.pes.dto.general.DemarcacionProjection;
import com.chc.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;
import com.chc.pes.service.general.DemarcacionService;

import com.chc.pes.util.DocumentWordUtils;
import com.chc.pes.util.Escenario;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.awt.*;
import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ReporteWordUtEscasezService {

    @Value("${file.report-dir}")
    private String reportDir;
    private final DemarcacionService demarcacionService;
    private final ReporteUtEscasezService reporteUtEscasezService;
    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;

    public ReporteWordUtEscasezService(DemarcacionService demarcacionService, ReporteUtEscasezService reporteUtEscasezService, IndicadorUtEscasezRepository indicadorUtEscasezRepository) {
        this.demarcacionService = demarcacionService;
        this.reporteUtEscasezService = reporteUtEscasezService;
        this.indicadorUtEscasezRepository = indicadorUtEscasezRepository;
    }

    public void generarReporteWord(Integer anio, Integer mes, String tipo) throws IOException {
        String archivoOrigen = reportDir + "/UTE_" + tipo + ".docx";
        String archivoFinal = reportDir + "/Reporte_UTE_" + tipo + ".docx";

        try (XWPFDocument document = new XWPFDocument(new FileInputStream(archivoOrigen))) {

            // Cambiar mes y año en el documento
            DocumentWordUtils.cambiarMesYAnioEnParrafo(document, mes, anio);

            // Obtener información de la demarcacion
            DemarcacionProjection demarcacionInfo = getDemarcacionInfo(tipo.toLowerCase());
            Integer demarcacionId = demarcacionInfo.getId();
            String demarcacionCodigo = demarcacionInfo.getCodigo();

            // Ruta del archivo SVG del mapa correspondiente
            String fileSvg = reportDir + "/svgMapaGeneral/" + demarcacionCodigo + "_UTE.svg";

            // Obtenemos los datos de escenario para las UTs de la demarcación para mes/año específicos
            List<IndicadorUTEscenarioProjection> listUTEscenario = indicadorUtEscasezRepository.findByAnioMesDemarcacionUTEscenario(anio, mes, demarcacionId);

            // Abrimos el archivo SVG y buscamos los códigos de las UTs para actualizar sus colores
            String svg = Files.readString(Path.of(fileSvg), StandardCharsets.UTF_8);
            String outputPngPath = reportDir + "/svgMapaGeneral/" + demarcacionCodigo + "_UTE.png";
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
                transcoder.transcode(input, output);

                try (FileOutputStream out = new FileOutputStream(archivoFinal)) {
                    document.write(out);
                }
            } catch (TranscoderException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String obtenerColorPorEscenarioUT(String escenario) {
        Escenario escenarioResult = Escenario.fromValue(escenario);
        return Escenario.getColor(escenarioResult);
    }

    private DemarcacionProjection getDemarcacionInfo(String ubicacion) {
        List<DemarcacionProjection> demarcacionesEscasez = demarcacionService.findDemarcacionesByTipo('E');
        return demarcacionesEscasez.stream()
                .filter(d -> d.getNombre().toLowerCase().contains(ubicacion))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la demarcación de tipo: " + ubicacion));
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

}

//
//private static void crearTablaPrincipal(XWPFTable table) {
//    // Configurar bordes de la tabla
//    table.setWidth("100%");
//
//    // FILA 0: Cabecera principal con colspan
//    XWPFTableRow row0 = table.getRow(0);
//    XWPFTableCell cell00 = row0.getCell(0);
//    cell00.setText("Reporte de Ventas 2025");
//    cell00.setColor("4472C4"); // Color de fondo azul
//
//    // Fusionar las 4 columnas de la primera fila (colspan=4)
//    cell00.getCTTc().addNewTcPr().addNewGridSpan().setVal(java.math.BigInteger.valueOf(4));
//
//    // Centrar texto
//    XWPFParagraph p0 = cell00.getParagraphs().get(0);
//    p0.setAlignment(ParagraphAlignment.CENTER);
//    XWPFRun r0 = p0.getRuns().isEmpty() ? p0.createRun() : p0.getRuns().get(0);
//    r0.setBold(true);
//    r0.setColor("FFFFFF");
//
//    // Eliminar celdas fusionadas
//    row0.removeCell(1);
//    row0.removeCell(1);
//    row0.removeCell(1);
//
//    // FILA 1: Subcabeceras
//    XWPFTableRow row1 = table.getRow(1);
//    configurarCeldaCabecera(row1.getCell(0), "Región");
//    configurarCeldaCabecera(row1.getCell(1), "Producto");
//    configurarCeldaCabecera(row1.getCell(2), "Cantidad");
//    configurarCeldaCabecera(row1.getCell(3), "Total");
//
//    // FILA 2: Primera región con rowspan
//    XWPFTableRow row2 = table.getRow(2);
//    XWPFTableCell cellNorte = row2.getCell(0);
//    cellNorte.setText("Norte");
//
//    // Fusionar 2 filas (rowspan=2)
//    cellNorte.getCTTc().addNewTcPr().addNewVMerge().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.RESTART);
//
//    row2.getCell(1).setText("Laptop");
//    row2.getCell(2).setText("150");
//    row2.getCell(3).setText("$225,000");
//
//    // FILA 3: Continuación región Norte
//    XWPFTableRow row3 = table.getRow(3);
//    XWPFTableCell cellNorte2 = row3.getCell(0);
//    cellNorte2.getCTTc().addNewTcPr().addNewVMerge().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.CONTINUE);
//
//    row3.getCell(1).setText("Mouse");
//    row3.getCell(2).setText("500");
//    row3.getCell(3).setText("$15,000");
//
//    // FILA 4: Segunda región
//    XWPFTableRow row4 = table.getRow(4);
//    row4.getCell(0).setText("Sur");
//    row4.getCell(1).setText("Teclado");
//    row4.getCell(2).setText("300");
//    row4.getCell(3).setText("$45,000");
//}
//
//private static void configurarCeldaCabecera(XWPFTableCell cell, String texto) {
//    cell.setText(texto);
//    cell.setColor("D9E1F2"); // Color de fondo gris claro
//
//    XWPFParagraph p = cell.getParagraphs().get(0);
//    p.setAlignment(ParagraphAlignment.CENTER);
//
//    if (!p.getRuns().isEmpty()) {
//        XWPFRun run = p.getRuns().get(0);
//        run.setBold(true);
//    }
//}
//

/// / Agregar salto de página
//            DocumentWordUtils.agregarSaltoDePagina(document);
//
//List<IndicadorUTFechaDataProjection> datos = reporteUtEscasezService.getTotalDataUTFecha(1, anio);
//List<Map<String, Object>> datosGrafico = DocumentWordUtils.prepararDatosTotalesParaGrafico(datos);
//
/// / Imprimir los datos del gráfico (para verificación)
//            for (Map<String, Object> dato : datosGrafico) {
//        System.out.println(dato);
//            }
//
//                    // Generar imagen del gráfico
//                    DocumentWordUtils.generarGraficoLineas(datosGrafico);
//
/// / Crear tablas debajo del título
//XWPFTable table = document.createTable(5, 4);
//crearTablaPrincipal(table);
//
//            DocumentWordUtils.encabezadoH2(document, "Nuevo Texto de Encabezado");
//
//            document.createParagraph();
//
//XWPFTable table1 = document.createTable(5, 4);
//crearTablaPrincipal(table1);
//
//XWPFParagraph saltoPagina_horizontal = document.createParagraph();
//            DocumentWordUtils.configurarOrientacionHorizontal(saltoPagina_horizontal);
//
//XWPFTable table2 = document.createTable(5, 4);
//crearTablaPrincipal(table2);
