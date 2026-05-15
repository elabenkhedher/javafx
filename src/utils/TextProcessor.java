/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author elabe
 */
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class TextProcessor {
    
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[^a-zA-Z0-9\\s\\.,;:!?\\-]");
    
    /**
     * Nettoie un texte en supprimant les caractères superflus
     * 
     * @param text Texte brut
     * @return Texte nettoyé
     */
    public static String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Supprimer les caractères de contrôle
        text = text.replaceAll("\\p{C}", " ");
        
        // Normaliser les espaces multiples
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");
        
        // Supprimer les espaces en début/fin de ligne
        text = text.lines()
                   .map(String::trim)
                   .filter(line -> !line.isEmpty())
                   .reduce((a, b) -> a + "\n" + b)
                   .orElse("");
        
        // Trim final
        return text.trim();
    }
    
    /**
     * Normalise le texte pour la recherche (minuscules, sans accents)
     * 
     * @param text Texte à normaliser
     * @return Texte normalisé
     */
    public static String normalizeForSearch(String text) {
        if (text == null) return "";
        
        // Convertir en minuscules
        text = text.toLowerCase();
        
        // Supprimer les accents
        text = removeAccents(text);
        
        // Supprimer la ponctuation excessive
        text = text.replaceAll("[\\.,;:!?]{2,}", " ");
        
        return text.trim();
    }
    
    /**
     * Supprime les accents d'un texte
     * 
     * @param text Texte avec accents
     * @return Texte sans accents
     */
    public static String removeAccents(String text) {
        if (text == null) return "";
        
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
    
    /**
     * Extrait les mots-clés d'un texte
     * 
     * @param text Texte source
     * @param minLength Longueur minimale des mots
     * @return Liste de mots-clés
     */
    public static List<String> extractKeywords(String text, int minLength) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Mots vides à ignorer
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "le", "la", "les", "un", "une", "des", "de", "du", "et", "ou", "mais",
            "pour", "par", "sur", "avec", "dans", "sans", "sous", "vers",
            "the", "a", "an", "and", "or", "but", "for", "by", "on", "with", "in"
        ));
        
        String[] words = normalizeForSearch(text).split("\\s+");
        List<String> keywords = new ArrayList<>();
        
        for (String word : words) {
            // Supprimer la ponctuation
            word = word.replaceAll("[^a-z0-9]", "");
            
            if (word.length() >= minLength && !stopWords.contains(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    /**
     * Compte la fréquence des mots dans un texte
     * 
     * @param text Texte à analyser
     * @return Map mot -> fréquence
     */
    public static Map<String, Integer> getWordFrequency(String text) {
        Map<String, Integer> frequency = new HashMap<>();
        
        List<String> keywords = extractKeywords(text, 3);
        
        for (String word : keywords) {
            frequency.put(word, frequency.getOrDefault(word, 0) + 1);
        }
        
        return frequency;
    }
    
    /**
     * Tronque un texte à une longueur maximale
     * 
     * @param text Texte à tronquer
     * @param maxLength Longueur maximale
     * @param addEllipsis Ajouter "..." à la fin
     * @return Texte tronqué
     */
    public static String truncate(String text, int maxLength, boolean addEllipsis) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        
        String truncated = text.substring(0, maxLength);
        
        if (addEllipsis) {
            truncated += "...";
        }
        
        return truncated;
    }
    
    /**
     * Détecte la langue d'un texte (simple heuristique)
     * 
     * @param text Texte à analyser
     * @return "fr", "en", ou "unknown"
     */
    public static String detectLanguage(String text) {
        if (text == null || text.length() < 50) {
            return "unknown";
        }
        
        text = text.toLowerCase();
        
        // Mots français courants
        String[] frenchWords = {"le", "la", "les", "de", "et", "pour", "dans", "sur", "avec", "être"};
        // Mots anglais courants
        String[] englishWords = {"the", "of", "and", "to", "in", "is", "for", "on", "with", "that"};
        
        int frenchCount = 0;
        int englishCount = 0;
        
        for (String word : frenchWords) {
            if (text.contains(" " + word + " ")) frenchCount++;
        }
        
        for (String word : englishWords) {
            if (text.contains(" " + word + " ")) englishCount++;
        }
        
        if (frenchCount > englishCount) {
            return "fr";
        } else if (englishCount > frenchCount) {
            return "en";
        }
        
        return "unknown";
    }
    
    /**
     * Extrait un résumé d'un texte long
     * 
     * @param text Texte complet
     * @param maxSentences Nombre maximum de phrases
     * @return Résumé
     */
    public static String summarize(String text, int maxSentences) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Séparer en phrases
        String[] sentences = text.split("[.!?]+");
        
        if (sentences.length <= maxSentences) {
            return text;
        }
        
        // Prendre les premières phrases
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < maxSentences && i < sentences.length; i++) {
            summary.append(sentences[i].trim()).append(". ");
        }
        
        return summary.toString().trim();
    }
    
    /**
     * Calcule la similarité entre deux textes (Jaccard)
     * 
     * @param text1 Premier texte
     * @param text2 Deuxième texte
     * @return Score de similarité (0.0 à 1.0)
     */
    public static double calculateSimilarity(String text1, String text2) {
        Set<String> words1 = new HashSet<>(extractKeywords(text1, 3));
        Set<String> words2 = new HashSet<>(extractKeywords(text2, 3));
        
        if (words1.isEmpty() && words2.isEmpty()) {
            return 1.0;
        }
        
        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }
        
        // Intersection
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        // Union
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return (double) intersection.size() / union.size();
    }
}
