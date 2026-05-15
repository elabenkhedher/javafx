/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author elabe
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateAnalysis {
    private int candidateId;
    private int jobId;
    private int score; // 0-100
    private List<String> competencesDetectees;
    private List<String> competencesManquantes;
    private String recommandation; // "Accepté", "Refusé", "À revoir"
    private Map<String, Integer> detailsScore; // compétence -> score individuel
    private String analysisDate;
    private String commentaire;
    
    // Constructeurs
    public CandidateAnalysis() {
        this.competencesDetectees = new ArrayList<>();
        this.competencesManquantes = new ArrayList<>();
        this.detailsScore = new HashMap<>();
    }
    
    public CandidateAnalysis(int candidateId, int jobId) {
        this();
        this.candidateId = candidateId;
        this.jobId = jobId;
    }
    
    // Méthodes utiles
    
    /**
     * Calcule le taux de correspondance
     * @return 
     */
    public double getTauxCorrespondance() {
        int total = competencesDetectees.size() + competencesManquantes.size();
        if (total == 0) return 0;
        return (competencesDetectees.size() * 100.0) / total;
    }
    
    /**
     * Retourne le nombre total de compétences évaluées
     * @return 
     */
    public int getTotalCompetences() {
        return competencesDetectees.size() + competencesManquantes.size();
    }
    
    /**
     * Vérifie si le candidat est recommandé
     * @return 
     */
    public boolean isRecommande() {
        return "Accepté".equalsIgnoreCase(recommandation);
    }
    
    /**
     * Génère un rapport textuel de l'analyse
     * @return 
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT D'ANALYSE ===\n\n");
        report.append("Score global: ").append(score).append("%\n");
        report.append("Recommandation: ").append(recommandation).append("\n");
        report.append("Taux de correspondance: ").append(String.format("%.1f", getTauxCorrespondance())).append("%\n\n");
        
        report.append("Compétences détectées (").append(competencesDetectees.size()).append("):\n");
        for (String comp : competencesDetectees) {
            int scoreComp = detailsScore.getOrDefault(comp, 0);
            report.append("  ✓ ").append(comp).append(" (").append(scoreComp).append("%)\n");
        }
        
        report.append("\nCompétences manquantes (").append(competencesManquantes.size()).append("):\n");
        for (String comp : competencesManquantes) {
            report.append("  ✗ ").append(comp).append("\n");
        }
        
        if (commentaire != null && !commentaire.isEmpty()) {
            report.append("\nCommentaire:\n").append(commentaire).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Retourne une note sous forme de lettre (A, B, C, D, F)
     * @return 
     */
    public String getNoteLettres() {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
    
    // Getters et Setters
    public int getCandidateId() {
        return candidateId;
    }
    
    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }
    
    public int getJobId() {
        return jobId;
    }
    
    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public List<String> getCompetencesDetectees() {
        return competencesDetectees;
    }
    
    public void setCompetencesDetectees(List<String> competencesDetectees) {
        this.competencesDetectees = competencesDetectees;
    }
    
    public List<String> getCompetencesManquantes() {
        return competencesManquantes;
    }
    
    public void setCompetencesManquantes(List<String> competencesManquantes) {
        this.competencesManquantes = competencesManquantes;
    }
    
    public String getRecommandation() {
        return recommandation;
    }
    
    public void setRecommandation(String recommandation) {
        this.recommandation = recommandation;
    }
    
    public Map<String, Integer> getDetailsScore() {
        return detailsScore;
    }
    
    public void setDetailsScore(Map<String, Integer> detailsScore) {
        this.detailsScore = detailsScore;
    }
    
    public String getAnalysisDate() {
        return analysisDate;
    }
    
    public void setAnalysisDate(String analysisDate) {
        this.analysisDate = analysisDate;
    }
    
    public String getCommentaire() {
        return commentaire;
    }
    
    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
    
    @Override
    public String toString() {
        return "CandidateAnalysis{" +
                "candidateId=" + candidateId +
                ", jobId=" + jobId +
                ", score=" + score +
                ", recommandation='" + recommandation + '\'' +
                '}';
    }
}