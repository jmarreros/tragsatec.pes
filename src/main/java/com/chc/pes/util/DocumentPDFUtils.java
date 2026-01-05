package com.chc.pes.util;

import org.docx4j.Docx4J;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.FileOutputStream;

public class DocumentPDFUtils {

    public void convertDocxToPdf(String docxPath, String pdfPath) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(docxPath));

        // Configurar el mapper de fuentes
        Mapper fontMapper = new BestMatchingMapper();
        wordMLPackage.setFontMapper(fontMapper);

        // Exportar a PDF
        try (FileOutputStream os = new FileOutputStream(pdfPath)) {
            Docx4J.toPDF(wordMLPackage, os);
        }
    }
//    public void convertDocxToPdf(String docxPath, String pdfPath) throws Exception {
//        File docxFile = new File(docxPath);
//        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxFile);
//
//        try (OutputStream out = new FileOutputStream(pdfPath)) {
//            Docx4J.toPDF(wordMLPackage, out);
//            out.flush();
//        }
//    }
}
