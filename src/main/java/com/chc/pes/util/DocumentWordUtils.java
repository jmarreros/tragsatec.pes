package com.chc.pes.util;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import java.math.BigInteger;
import java.util.List;

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

}


//            for (XWPFTable table : document.getTables()) {
//                for (XWPFTableRow row : table.getRows()) {
//                    for (XWPFTableCell cell : row.getTableCells()) {
//                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
//                            reemplazarTextoEnParagrafo(paragraph, "<<mes>>", nombreMes);
//                            reemplazarTextoEnParagrafo(paragraph, "<<año>>", String.valueOf(anio));
//                        }
//                    }
//                }
//            }
