package com.couchbase.se.services;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class PDF {
    private PDDocument pdDocument;
    private PDFTextStripperByArea pdfTextStripperByArea;
    private PDFTextStripper pdfTextStripper;
    private String filename;

    public PDF(String filename) {
        try {
            this.filename = filename;
            this.pdDocument = Loader.loadPDF(new File(filename));
            this.pdfTextStripperByArea = new PDFTextStripperByArea();
            this.pdfTextStripperByArea.setSortByPosition(true);
            this.pdfTextStripper = new PDFTextStripper();
            this.pdfTextStripper.setSortByPosition(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTextByArea(int x, int y, int width, int height) {
        Rectangle rectangle = new Rectangle(x, y, width, height);
        this.pdfTextStripperByArea.addRegion("region", rectangle);
        PDPage firstPage = this.pdDocument.getPage(0);
        try {
            this.pdfTextStripperByArea.extractRegions(firstPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.pdfTextStripperByArea.getTextForRegion("region").replace("\n", "").replace("\r", "");
    }

    public String getAllText() {
        String pdfText = null;
        this.pdfTextStripper.setStartPage(0);
        this.pdfTextStripper.setEndPage(1);
        try {
            pdfText = this.pdfTextStripper.getText(this.pdDocument);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfText;
    }

    public String pdfToBase64() {
        byte[] fileContent = new byte[0];
        try {
            fileContent = FileUtils.readFileToByteArray(new File(this.filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encodedString = Base64.getEncoder().encodeToString(fileContent);
        return encodedString;

    }
}
