package model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Job {
    private int id;
    private String titre;
    private String description;
    private String competencesRequises;
    private String departement;
    private String statut;
    private String dateCreation;
    private int salaireMini;
    private int salaireMaxi;

    public Job() {
        this.statut = "Ouverte";
    }

    public Job(String titre, String description, String competencesRequises, String departement) {
        this();
        this.titre = titre;
        this.description = description;
        this.competencesRequises = competencesRequises;
        this.departement = departement;
    }

    /**
     * Retourne les compétences sous forme de liste
     * @return 
     */
    public List<String> getCompetencesAsList() {
        if (competencesRequises == null || competencesRequises.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(competencesRequises.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return titre + " - " + departement;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCompetencesRequises() { return competencesRequises; }
    public void setCompetencesRequises(String competencesRequises) { this.competencesRequises = competencesRequises; }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }

    public int getSalaireMini() { return salaireMini; }
    public void setSalaireMini(int salaireMini) { this.salaireMini = salaireMini; }

    public int getSalaireMaxi() { return salaireMaxi; }
    public void setSalaireMaxi(int salaireMaxi) { this.salaireMaxi = salaireMaxi; }
}
