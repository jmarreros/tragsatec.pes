package com.chc.pes.util;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class DocumentPDFUtils {

    public void convertDocxToPdf(String docxPath, String pdfPath) throws Exception {
        File docxFile = new File(docxPath);
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxFile);

        try (OutputStream out = new FileOutputStream(pdfPath)) {
            Docx4J.toPDF(wordMLPackage, out);
            out.flush();
        }
    }
}
