/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author elabe
 */
public class Candidate {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String cvText;
    private String cvFilePath;
    private String dateCandidature;
    private String adresse;
    private String linkedinUrl;
    private Job jobApplication; // transient field to store selected job in dialog
    
    // Constructeurs
    public Candidate() {}
    
    public Candidate(String nom, String prenom, String email, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
    }
    
    public Candidate(String nom, String prenom, String email, String telephone, String cvText) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.cvText = cvText;
    }
    
    // Méthodes utiles
    
    /**
     * Retourne le nom complet
     * @return 
     */
    public String getNomComplet() {
        return prenom + " " + nom;
    }
    
    /**
     * Vérifie si le candidat a un CV
     * @return 
     */
    public boolean hasCV() {
        return cvText != null && !cvText.trim().isEmpty();
    }
    
    /**
     * Vérifie si le CV est valide (au moins 50 caractères)
     * @return 
     */
    public boolean isCVValid() {
        return hasCV() && cvText.length() >= 50;
    }
    
    /**
     * Retourne un extrait du CV
     * @param maxLength
     * @return 
     */
    public String getCVExtrait(int maxLength) {
        if (!hasCV()) {
            return "Aucun CV disponible";
        }
        if (cvText.length() <= maxLength) {
            return cvText;
        }
        return cvText.substring(0, maxLength) + "...";
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getCvText() {
        return cvText;
    }
    
    public void setCvText(String cvText) {
        this.cvText = cvText;
    }
    
    public String getCvFilePath() {
        return cvFilePath;
    }
    
    public void setCvFilePath(String cvFilePath) {
        this.cvFilePath = cvFilePath;
    }
    
    public String getDateCandidature() {
        return dateCandidature;
    }
    
    public void setDateCandidature(String dateCandidature) {
        this.dateCandidature = dateCandidature;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public String getLinkedinUrl() {
        return linkedinUrl;
    }
    
    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public Job getJobApplication() {
        return jobApplication;
    }

    public void setJobApplication(Job jobApplication) {
        this.jobApplication = jobApplication;
    }
    
    @Override
    public String toString() {
        return "Candidate{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
