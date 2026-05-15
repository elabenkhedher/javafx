/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author elabe
 */
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;

public class PDFExtractor {
    
    /**
     * Extrait le texte brut d'un fichier PDF
     * 
     * @param filePath Chemin du fichier PDF
     * @return Texte extrait
     * @throws IOException Si erreur de lecture
     */
    public static String extractTextFromPDF(String filePath) throws IOException {
        File file = new File(filePath);
        
        if (!file.exists()) {
            throw new IOException("Fichier non trouvé: " + filePath);
        }
        
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Le fichier n'est pas un PDF: " + filePath);
        }
        
        try (PDDocument document = Loader.loadPDF(file)) {
            if (document.isEncrypted()) {
                throw new IOException("Le PDF est crypté et ne peut pas être lu");
            }
            
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            
            return stripper.getText(document);
        }
    }
    
    /**
     * Extrait et nettoie le texte d'un PDF
     * 
     * @param filePath Chemin du fichier
     * @return Texte nettoyé
     * @throws IOException Si erreur
     */
    public static String extractAndCleanText(String filePath) throws IOException {
        String rawText = extractTextFromPDF(filePath);
        return TextProcessor.cleanText(rawText);
    }
    
    /**
     * Extrait le texte d'une plage de pages spécifique
     * 
     * @param filePath Chemin du fichier
     * @param startPage Page de début (1-indexed)
     * @param endPage Page de fin (1-indexed)
     * @return Texte extrait
     * @throws IOException Si erreur
     */
    public static String extractTextFromPages(String filePath, int startPage, int endPage) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            
            return stripper.getText(document);
        }
    }
    
    /**
     * Retourne le nombre de pages d'un PDF
     * 
     * @param filePath Chemin du fichier
     * @return Nombre de pages
     * @throws IOException Si erreur
     */
    public static int getPageCount(String filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            return document.getNumberOfPages();
        }
    }
    
    /**
     * Valide qu'un fichier est un PDF lisible
     * 
     * @param filePath Chemin du fichier
     * @return true si valide, false sinon
     */
    public static boolean isValidPDF(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !filePath.toLowerCase().endsWith(".pdf")) {
                return false;
            }
            
            try (PDDocument document = Loader.loadPDF(file)) {
                return !document.isEncrypted() && document.getNumberOfPages() > 0;
            }
        } catch (IOException e) {
            return false;
        }
    }
}
