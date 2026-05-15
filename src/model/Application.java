/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author elabe
 */

public class Application {
    private int id;
    private int candidateId;
    private int jobId;
    private int score;
    private String recommandation;
    private String dateCandidature;
    private String statut; // "En attente", "Analysée", "Acceptée", "Refusée"
    private String notes;
    
    // Constructeurs
    public Application() {
        this.statut = "En attente";
    }
    
    public Application(int candidateId, int jobId) {
        this();
        this.candidateId = candidateId;
        this.jobId = jobId;
    }
    
    // Méthodes utiles
    
    /**
     * Vérifie si l'application a été analysée
     * @return 
     */
    public boolean isAnalysee() {
        return score > 0;
    }
    
    /**
     * Vérifie si le candidat est accepté
     * @return 
     */
    public boolean isAcceptee() {
        return "Accepté".equalsIgnoreCase(recommandation) || 
               "Acceptée".equalsIgnoreCase(statut);
    }
    
    /**
     * Accepte la candidature
     */
    public void accepter() {
        this.statut = "Acceptée";
        this.recommandation = "Accepté";
    }
    
    /**
     * Refuse la candidature
     */
    public void refuser() {
        this.statut = "Refusée";
        this.recommandation = "Refusé";
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
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
    
    public String getRecommandation() {
        return recommandation;
    }
    
    public void setRecommandation(String recommandation) {
        this.recommandation = recommandation;
    }
    
    public String getDateCandidature() {
        return dateCandidature;
    }
    
    public void setDateCandidature(String dateCandidature) {
        this.dateCandidature = dateCandidature;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", candidateId=" + candidateId +
                ", jobId=" + jobId +
                ", score=" + score +
                ", statut='" + statut + '\'' +
                '}';
    }
}