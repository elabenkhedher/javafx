/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author elabe
 */  

import model.*;
import java.util.*;
import java.util.regex.*;

public class CVAnalyzerService {
    
    // Dictionnaire de synonymes pour améliorer la détection
    private static final Map<String, List<String>> SYNONYMS = new HashMap<>();
    
    // Pondération des compétences (optionnel - toutes à 1.0 par défaut)
    private static final Map<String, Double> COMPETENCE_WEIGHTS = new HashMap<>();
    
    static {
        // Synonymes pour langages de programmation
        SYNONYMS.put("java", Arrays.asList("java", "j2ee", "jee", "java ee", "jakarta ee"));
        SYNONYMS.put("javascript", Arrays.asList("javascript", "js", "ecmascript", "es6", "es2015"));
        SYNONYMS.put("python", Arrays.asList("python", "py", "python3"));
        SYNONYMS.put("c++", Arrays.asList("c++", "cpp", "cplusplus"));
        SYNONYMS.put("c#", Arrays.asList("c#", "csharp", "c sharp"));
        
        // Synonymes pour frameworks
        SYNONYMS.put("spring", Arrays.asList("spring", "spring boot", "springboot", "spring framework"));
        SYNONYMS.put("react", Arrays.asList("react", "reactjs", "react.js"));
        SYNONYMS.put("angular", Arrays.asList("angular", "angularjs", "angular.js"));
        SYNONYMS.put("node.js", Arrays.asList("node.js", "nodejs", "node"));
        
        // Synonymes pour bases de données
        SYNONYMS.put("sql", Arrays.asList("sql", "mysql", "postgresql", "postgres", "oracle", "mssql", "mariadb"));
        SYNONYMS.put("mongodb", Arrays.asList("mongodb", "mongo"));
        
        // Synonymes pour outils
        SYNONYMS.put("git", Arrays.asList("git", "github", "gitlab", "bitbucket"));
        SYNONYMS.put("docker", Arrays.asList("docker", "containerization"));
        SYNONYMS.put("kubernetes", Arrays.asList("kubernetes", "k8s"));
        
        // Synonymes pour méthodologies
        SYNONYMS.put("agile", Arrays.asList("agile", "scrum", "kanban", "méthodologie agile"));
        SYNONYMS.put("machine learning", Arrays.asList("machine learning", "ml", "apprentissage automatique", "deep learning"));
    }
    
    /**
     * Analyse un CV par rapport à une offre d'emploi
     * 
     * @param candidate Le candidat avec son CV
     * @param job L'offre d'emploi avec les compétences requises
     * @return L'analyse complète
     */
    public CandidateAnalysis analyzeCV(Candidate candidate, Job job) {
        CandidateAnalysis analysis = new CandidateAnalysis(candidate.getId(), job.getId());
        
        String cvText = candidate.getCvText().toLowerCase();
        List<String> requiredSkills = job.getCompetencesAsList();
        
        List<String> detectedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        Map<String, Integer> detailsScore = new HashMap<>();
        
        int totalScore = 0;
        int maxPossibleScore = 0;
        
        // Analyse de chaque compétence requise
        for (String skill : requiredSkills) {
            skill = skill.trim().toLowerCase();
            
            int skillScore = analyzeSkill(cvText, skill);
            int weight = 100; // Poids par défaut
            
            maxPossibleScore += weight;
            
            if (skillScore > 0) {
                detectedSkills.add(skill);
                totalScore += (skillScore * weight) / 100;
            } else {
                missingSkills.add(skill);
            }
            
            detailsScore.put(skill, skillScore);
        }
        
        // Calcul du score global
        int globalScore = maxPossibleScore > 0 ? (totalScore * 100) / maxPossibleScore : 0;
        
        // Déterminer la recommandation
        String recommandation = determineRecommandation(globalScore, detectedSkills.size(), requiredSkills.size());
        
        // Remplir l'analyse
        analysis.setScore(globalScore);
        analysis.setCompetencesDetectees(detectedSkills);
        analysis.setCompetencesManquantes(missingSkills);
        analysis.setRecommandation(recommandation);
        analysis.setDetailsScore(detailsScore);
        analysis.setAnalysisDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        // Ajouter un commentaire automatique
        analysis.setCommentaire(generateAutoComment(analysis));
        
        return analysis;
    }
    
    /**
     * Analyse une compétence spécifique dans le CV
     * 
     * @param cvText Texte du CV en minuscules
     * @param skill Compétence à rechercher
     * @return Score de 0 à 100
     */
    private int analyzeSkill(String cvText, String skill) {
        int baseScore = 0;
        boolean found = false;
        
        // 1. Vérification directe
        if (cvText.contains(skill)) {
            found = true;
            baseScore = 100;
        } else {
            // 2. Vérification avec synonymes
            List<String> synonyms = SYNONYMS.getOrDefault(skill, Collections.singletonList(skill));
            for (String synonym : synonyms) {
                if (cvText.contains(synonym)) {
                    found = true;
                    baseScore = 90; // Score légèrement inférieur pour synonyme
                    break;
                }
            }
        }
        
        if (!found) {
            return 0;
        }
        
        // 3. Bonus pour l'expérience
        int experienceBonus = detectExperience(cvText, skill);
        baseScore = Math.min(100, baseScore + experienceBonus);
        
        // 4. Bonus pour certifications
        if (detectCertification(cvText, skill)) {
            baseScore = Math.min(100, baseScore + 10);
        }
        
        // 5. Bonus pour mention dans un titre/poste
        if (detectInTitle(cvText, skill)) {
            baseScore = Math.min(100, baseScore + 5);
        }
        
        return baseScore;
    }
    
    /**
     * Détecte l'expérience pour une compétence donnée
     * Retourne un bonus basé sur les années d'expérience
     * 
     * @param cvText Texte du CV
     * @param skill Compétence
     * @return Bonus de 0 à 20 points
     */
    private int detectExperience(String cvText, String skill) {
        // Patterns pour détecter l'expérience
        String[] patterns = {
            "(\\d+)\\s*(ans?|années?|years?)\\s*.*?" + Pattern.quote(skill),
            "(\\d+)\\s*\\+\\s*(ans?|années?|years?)\\s*.*?" + Pattern.quote(skill),
            Pattern.quote(skill) + "\\s*.*?(\\d+)\\s*(ans?|années?|years?)",
            "expérience.*?" + Pattern.quote(skill) + ".*?(\\d+)\\s*(ans?|années?|years?)"
        };
        
        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(cvText);
            
            if (matcher.find()) {
                try {
                    int years = Integer.parseInt(matcher.group(1));
                    // +5 points par année, maximum 20 points
                    return Math.min(20, years * 5);
                } catch (NumberFormatException e) {
                    // Ignorer
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Détecte si une certification est mentionnée
     */
    private boolean detectCertification(String cvText, String skill) {
        String[] certKeywords = {"certifié", "certified", "certification", "diplôme", "certificate"};
        
        for (String keyword : certKeywords) {
            if (cvText.contains(keyword) && cvText.contains(skill)) {
                // Vérifier la proximité
                int keywordIndex = cvText.indexOf(keyword);
                int skillIndex = cvText.indexOf(skill);
                if (Math.abs(keywordIndex - skillIndex) < 100) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Détecte si la compétence est mentionnée dans un titre de poste
     */
    private boolean detectInTitle(String cvText, String skill) {
        String[] titleKeywords = {"développeur", "developer", "ingénieur", "engineer", "chef", "manager", "expert", "specialist"};
        
        for (String title : titleKeywords) {
            if (cvText.contains(title) && cvText.contains(skill)) {
                int titleIndex = cvText.indexOf(title);
                int skillIndex = cvText.indexOf(skill);
                if (Math.abs(titleIndex - skillIndex) < 50) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Détermine la recommandation finale
     */
    private String determineRecommandation(int score, int detectedCount, int totalCount) {
        // Critères multiples
        double matchRate = (detectedCount * 100.0) / totalCount;
        
        if (score >= 80 && matchRate >= 70) {
            return "Accepté";
        } else if (score >= 50 && matchRate >= 50) {
            return "À revoir";
        } else {
            return "Refusé";
        }
    }
    
    /**
     * Génère un commentaire automatique basé sur l'analyse
     */
    private String generateAutoComment(CandidateAnalysis analysis) {
        StringBuilder comment = new StringBuilder();
        
        int score = analysis.getScore();
        int detected = analysis.getCompetencesDetectees().size();
        int total = analysis.getTotalCompetences();
        
        if (score >= 80) {
            comment.append("Excellent profil ! ");
        } else if (score >= 60) {
            comment.append("Bon profil avec quelques lacunes. ");
        } else if (score >= 40) {
            comment.append("Profil moyen nécessitant une formation. ");
        } else {
            comment.append("Profil ne correspondant pas aux critères. ");
        }
        
        comment.append(String.format("Le candidat possède %d/%d des compétences requises. ", detected, total));
        
        if (!analysis.getCompetencesManquantes().isEmpty()) {
            comment.append("Compétences à développer: ");
            comment.append(String.join(", ", analysis.getCompetencesManquantes().subList(
                0, Math.min(3, analysis.getCompetencesManquantes().size())
            )));
            if (analysis.getCompetencesManquantes().size() > 3) {
                comment.append("...");
            }
        }
        
        return comment.toString();
    }
    
    /**
     * Analyse multiple candidats pour un job et les trie par score
     * 
     * @param candidates Liste des candidats
     * @param job Offre d'emploi
     * @return Liste d'analyses triées par score décroissant
     */
    public List<CandidateAnalysis> analyzeAndRankCandidates(List<Candidate> candidates, Job job) {
        List<CandidateAnalysis> analyses = new ArrayList<>();
        
        for (Candidate candidate : candidates) {
            // Vérifier que le candidat a un CV
            if (candidate.hasCV()) {
                CandidateAnalysis analysis = analyzeCV(candidate, job);
                analyses.add(analysis);
            }
        }
        
        // Tri par score décroissant
        analyses.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        
        return analyses;
    }
    
    /**
     * Retourne des statistiques sur l'analyse
     * @param analyses
     * @return 
     */
    public Map<String, Object> getAnalysisStatistics(List<CandidateAnalysis> analyses) {
        Map<String, Object> stats = new HashMap<>();
        
        if (analyses.isEmpty()) {
            return stats;
        }
        
        int total = analyses.size();
        int accepted = (int) analyses.stream().filter(a -> "Accepté".equals(a.getRecommandation())).count();
        int rejected = (int) analyses.stream().filter(a -> "Refusé".equals(a.getRecommandation())).count();
        int toReview = total - accepted - rejected;
        
        double avgScore = analyses.stream().mapToInt(CandidateAnalysis::getScore).average().orElse(0);
        int maxScore = analyses.stream().mapToInt(CandidateAnalysis::getScore).max().orElse(0);
        int minScore = analyses.stream().mapToInt(CandidateAnalysis::getScore).min().orElse(0);
        
        stats.put("total", total);
        stats.put("accepted", accepted);
        stats.put("rejected", rejected);
        stats.put("toReview", toReview);
        stats.put("avgScore", avgScore);
        stats.put("maxScore", maxScore);
        stats.put("minScore", minScore);
        stats.put("acceptanceRate", (accepted * 100.0) / total);
        
        return stats;
    }
}