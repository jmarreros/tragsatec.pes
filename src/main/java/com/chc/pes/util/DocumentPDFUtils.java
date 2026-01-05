package com.chc.pes.util;

import org.docx4j.Docx4J;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.FileOutputStream;
import java.net.URL;

public class DocumentPDFUtils {

    static {
        try {
            URL calibriUrl = DocumentPDFUtils.class.getClassLoader().getResource("fonts/calibri.ttf");
            URL calibriBoldUrl = DocumentPDFUtils.class.getClassLoader().getResource("fonts/calibri_bold.ttf");

            if (calibriUrl != null) {
                PhysicalFonts.addPhysicalFont(calibriUrl.toURI());
            }
            if (calibriBoldUrl != null) {
                PhysicalFonts.addPhysicalFont(calibriBoldUrl.toURI());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar las fuentes para PDF", e);
        }
    }

    public void convertDocxToPdf(String docxPath, String pdfPath) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(docxPath));

        Mapper fontMapper = new BestMatchingMapper();
        wordMLPackage.setFontMapper(fontMapper);

        try (FileOutputStream os = new FileOutputStream(pdfPath)) {
            Docx4J.toPDF(wordMLPackage, os);
        }
    }
}
