/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author elabe
 */
import model.*;
import service.*;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AIAnalysisController {
    
    @FXML private ComboBox<Job> jobComboBox;
    @FXML private Button analyzeButton;
    @FXML private ProgressIndicator progressIndicator;
    
    @FXML private TableView<CandidateAnalysisRow> resultsTable;
    @FXML private TableColumn<CandidateAnalysisRow, Integer> colRank;
    @FXML private TableColumn<CandidateAnalysisRow, String> colCandidateName;
    @FXML private TableColumn<CandidateAnalysisRow, Integer> colScore;
    @FXML private TableColumn<CandidateAnalysisRow, String> colRecommandation;
    @FXML private TableColumn<CandidateAnalysisRow, String> colMatch;
    
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ToggleGroup filterGroup;
    @FXML private Label lblResultCount;
    
    @FXML private VBox candidateCard;
    @FXML private Label lblCandidateName;
    @FXML private Label lblCandidateEmail;
    @FXML private Label lblScoreValue;
    @FXML private ProgressBar scoreProgressBar;
    @FXML private Label lblRecommendation;
    @FXML private VBox detectedSkillsContainer;
    @FXML private VBox missingSkillsContainer;
    @FXML private TextArea detailsTextArea;
    
    @FXML private VBox chartContainer;
    @FXML private BarChart<String, Number> scoresChart;
    @FXML private VBox placeholderBox;
    
    private CVAnalyzerService analyzerService;
    private ObservableList<CandidateAnalysisRow> analysisData;
    private ObservableList<CandidateAnalysisRow> filteredData;
    
    @FXML
    public void initialize() {
        analyzerService = new CVAnalyzerService();
        analysisData = FXCollections.observableArrayList();
        filteredData = FXCollections.observableArrayList();
        
        setupTable();
        loadJobs();
        setupFiltersAndSort();
        
        // Cacher la carte candidat au départ et montrer le placeholder
        if (candidateCard != null) {
            candidateCard.setManaged(false);
            candidateCard.setVisible(false);
        }
        if (placeholderBox != null) {
            placeholderBox.setManaged(true);
            placeholderBox.setVisible(true);
        }
    }
    
    /**
     * Configuration de la table de résultats
     */
    private void setupTable() {
        colRank.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                resultsTable.getItems().indexOf(cellData.getValue()) + 1
            ).asObject()
        );
        
        colCandidateName.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCandidateName())
        );
        
        colScore.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getScore()).asObject()
        );
        
        colRecommandation.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRecommandation())
        );
        
        colMatch.setCellValueFactory(cellData -> {
            int detected = cellData.getValue().getAnalysis().getCompetencesDetectees().size();
            int total = cellData.getValue().getAnalysis().getCompetencesDetectees().size() + 
                       cellData.getValue().getAnalysis().getCompetencesManquantes().size();
            return new javafx.beans.property.SimpleStringProperty(detected + "/" + total);
        });
        
        // Style pour la recommandation
        colRecommandation.setCellFactory(column -> new TableCell<CandidateAnalysisRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Accepté":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; " +
                                   "-fx-font-weight: bold; -fx-padding: 5;");
                            break;
                        case "Refusé":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; " +
                                   "-fx-font-weight: bold; -fx-padding: 5;");
                            break;
                        case "À revoir":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; " +
                                   "-fx-font-weight: bold; -fx-padding: 5;");
                            break;
                    }
                }
            }
        });
        
        // Style pour le score
        colScore.setCellFactory(column -> new TableCell<CandidateAnalysisRow, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + "%");
                    if (item >= 80) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (item >= 50) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        resultsTable.setItems(filteredData);
        
        // Sélection d'une ligne
        resultsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showAnalysisDetails(newSelection);
                }
            }
        );
    }
    
    /**
     * Charge les offres disponibles
     */
    private void loadJobs() {
        ObservableList<Job> jobs = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM job WHERE statut = 'Ouverte' ORDER BY id DESC";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Job job = new Job();
                    job.setId(rs.getInt("id"));
                    job.setTitre(rs.getString("titre"));
                    job.setDescription(rs.getString("description"));
                    job.setCompetencesRequises(rs.getString("competences_requises"));
                    job.setDepartement(rs.getString("departement"));
                    job.setStatut(rs.getString("statut"));
                    
                    jobs.add(job);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        jobComboBox.setItems(jobs);
        
        // Affichage personnalisé dans le ComboBox
        jobComboBox.setCellFactory(param -> new ListCell<Job>() {
            @Override
            protected void updateItem(Job item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitre() + " - " + item.getDepartement());
                }
            }
        });
        
        jobComboBox.setButtonCell(new ListCell<Job>() {
            @Override
            protected void updateItem(Job item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitre() + " - " + item.getDepartement());
                }
            }
        });
    }
    
    /**
     * Configuration des filtres et du tri
     */
    private void setupFiltersAndSort() {
        // Tri
        if (sortComboBox != null) {
            sortComboBox.setOnAction(e -> applySortAndFilter());
        }
        
        // Filtres
        if (filterGroup != null) {
            filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                applySortAndFilter();
            });
        }
    }
    
    /**
     * Applique le tri et les filtres
     */
    private void applySortAndFilter() {
        filteredData.clear();
        
        // Filtrer
        String filter = "Tous";
        if (filterGroup != null && filterGroup.getSelectedToggle() != null) {
            RadioButton selected = (RadioButton) filterGroup.getSelectedToggle();
            filter = selected.getText();
        }
        
        List<CandidateAnalysisRow> filtered = new ArrayList<>();
        for (CandidateAnalysisRow row : analysisData) {
            if ("Tous".equals(filter)) {
                filtered.add(row);
            } else if ("Acceptés".equals(filter) && "Accepté".equals(row.getRecommandation())) {
                filtered.add(row);
            } else if ("Refusés".equals(filter) && "Refusé".equals(row.getRecommandation())) {
                filtered.add(row);
            }
        }
        
        // Trier
        String sort = sortComboBox != null ? sortComboBox.getValue() : "Score (décroissant)";
        if (sort != null) {
            switch (sort) {
                case "Score (décroissant)":
                    filtered.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
                    break;
                case "Score (croissant)":
                    filtered.sort((a, b) -> Integer.compare(a.getScore(), b.getScore()));
                    break;
                case "Nom (A-Z)":
                    filtered.sort((a, b) -> a.getCandidateName().compareTo(b.getCandidateName()));
                    break;
            }
        }
        
        filteredData.addAll(filtered);
        
        if (lblResultCount != null) {
            lblResultCount.setText(filteredData.size() + " candidats analysés");
        }
    }
    
    /**
     * Lancer l'analyse
     */
    @FXML
    private void handleAnalyze() {
        Job selectedJob = jobComboBox.getValue();
        
        if (selectedJob == null) {
            showAlert("Erreur", "Veuillez sélectionner une offre d'emploi", Alert.AlertType.ERROR);
            return;
        }
        
        // Récupérer les candidats
        List<Candidate> candidates = getCandidatesForJob(selectedJob.getId());
        
        if (candidates.isEmpty()) {
            showAlert("Information", "Aucun candidat trouvé pour cette offre", Alert.AlertType.INFORMATION);
            return;
        }
        
        // Tâche asynchrone pour l'analyse
        Task<List<CandidateAnalysis>> analysisTask = new Task<>() {
            @Override
            protected List<CandidateAnalysis> call() throws Exception {
                return analyzerService.analyzeAndRankCandidates(candidates, selectedJob);
            }
        };
        
        // Afficher la progression
        progressIndicator.setVisible(true);
        analyzeButton.setDisable(true);
        
        analysisTask.setOnSucceeded(event -> {
            List<CandidateAnalysis> analyses = analysisTask.getValue();
            displayResults(analyses, candidates);
            progressIndicator.setVisible(false);
            analyzeButton.setDisable(false);
            
            // Sauvegarder dans la DB
            saveAnalysesToDatabase(analyses);
            
            // Afficher le graphique
            showChart(analyses, candidates);
        });
        
        analysisTask.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            analyzeButton.setDisable(false);
            showAlert("Erreur", "Erreur lors de l'analyse", Alert.AlertType.ERROR);
        });
        
        new Thread(analysisTask).start();
    }
    
    /**
     * Récupère les candidats pour un job donné
     */
    private List<Candidate> getCandidatesForJob(int jobId) {
        List<Candidate> candidates = new ArrayList<>();
        
        try (Connection conn = DatabaseService.getConnection()) {
            // On cherche d'abord les candidats qui ont déjà postulé à cette offre
            String sqlApplied = "SELECT c.* FROM candidate c " +
                               "JOIN application a ON c.id = a.candidate_id " +
                               "WHERE a.job_id = ? AND c.cv_text IS NOT NULL AND c.cv_text != ''";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlApplied)) {
                pstmt.setInt(1, jobId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    candidates.add(mapResultSetToCandidate(rs));
                }
            }
            
            // Si aucun candidat n'a postulé, on prend tous les candidats avec un CV (pour test/démo)
            if (candidates.isEmpty()) {
                String sqlAll = "SELECT * FROM candidate WHERE cv_text IS NOT NULL AND cv_text != ''";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sqlAll)) {
                    while (rs.next()) {
                        candidates.add(mapResultSetToCandidate(rs));
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return candidates;
    }
    
    private Candidate mapResultSetToCandidate(ResultSet rs) throws SQLException {
        Candidate candidate = new Candidate();
        candidate.setId(rs.getInt("id"));
        candidate.setNom(rs.getString("nom"));
        candidate.setPrenom(rs.getString("prenom"));
        candidate.setEmail(rs.getString("email"));
        candidate.setTelephone(rs.getString("telephone"));
        candidate.setCvText(rs.getString("cv_text"));
        candidate.setCvFilePath(rs.getString("cv_file_path"));
        return candidate;
    }
    
    /**
     * Affiche les résultats de l'analyse
     */
    private void displayResults(List<CandidateAnalysis> analyses, List<Candidate> candidates) {
        analysisData.clear();
        
        for (CandidateAnalysis analysis : analyses) {
            Candidate candidate = candidates.stream()
                .filter(c -> c.getId() == analysis.getCandidateId())
                .findFirst()
                .orElse(null);
            
            if (candidate != null) {
                CandidateAnalysisRow row = new CandidateAnalysisRow(
                    candidate.getPrenom() + " " + candidate.getNom(),
                    candidate.getEmail(),
                    analysis.getScore(),
                    analysis.getRecommandation(),
                    analysis
                );
                analysisData.add(row);
            }
        }
        
        applySortAndFilter();
    }
    
    /**
     * Affiche les détails d'une analyse
     */
    private void showAnalysisDetails(CandidateAnalysisRow row) {
        CandidateAnalysis analysis = row.getAnalysis();
        
        // Afficher la carte et cacher le placeholder
        if (candidateCard != null) {
            candidateCard.setManaged(true);
            candidateCard.setVisible(true);
        }
        if (placeholderBox != null) {
            placeholderBox.setManaged(false);
            placeholderBox.setVisible(false);
        }
        
        // Informations candidat
        lblCandidateName.setText(row.getCandidateName());
        lblCandidateEmail.setText(row.getEmail());
        
        // Score
        lblScoreValue.setText(analysis.getScore() + "%");
        scoreProgressBar.setProgress(analysis.getScore() / 100.0);
        
        // Style du score
        if (analysis.getScore() >= 80) {
            lblScoreValue.setStyle("-fx-text-fill: #2ecc71;");
        } else if (analysis.getScore() >= 50) {
            lblScoreValue.setStyle("-fx-text-fill: #f39c12;");
        } else {
            lblScoreValue.setStyle("-fx-text-fill: #e74c3c;");
        }
        
        // Recommandation
        lblRecommendation.setText(analysis.getRecommandation());
        switch (analysis.getRecommandation()) {
            case "Accepté":
                lblRecommendation.setStyle("-fx-background-color: #2ecc71;");
                break;
            case "Refusé":
                lblRecommendation.setStyle("-fx-background-color: #e74c3c;");
                break;
            case "À revoir":
                lblRecommendation.setStyle("-fx-background-color: #f39c12;");
                break;
        }
        
        // Compétences détectées
        detectedSkillsContainer.getChildren().clear();
        for (String skill : analysis.getCompetencesDetectees()) {
            int skillScore = analysis.getDetailsScore().get(skill);
            Label skillLabel = new Label("✓ " + skill + " (" + skillScore + "%)");
            skillLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            detectedSkillsContainer.getChildren().add(skillLabel);
        }
        
        // Compétences manquantes
        missingSkillsContainer.getChildren().clear();
        for (String skill : analysis.getCompetencesManquantes()) {
            Label skillLabel = new Label("✗ " + skill);
            skillLabel.setStyle("-fx-text-fill: #e74c3c;");
            missingSkillsContainer.getChildren().add(skillLabel);
        }
        
        // Détails texte
        StringBuilder details = new StringBuilder();
        details.append("=== ANALYSE DÉTAILLÉE ===\n\n");
        details.append("Candidat: ").append(row.getCandidateName()).append("\n");
        details.append("Email: ").append(row.getEmail()).append("\n\n");
        details.append("Score global: ").append(analysis.getScore()).append("%\n");
        details.append("Recommandation: ").append(analysis.getRecommandation()).append("\n\n");
        
        details.append("Compétences détectées (").append(analysis.getCompetencesDetectees().size()).append("):\n");
        for (String skill : analysis.getCompetencesDetectees()) {
            int score = analysis.getDetailsScore().get(skill);
            details.append("  ✓ ").append(skill).append(" - ").append(score).append("%\n");
        }
        
        details.append("\nCompétences manquantes (").append(analysis.getCompetencesManquantes().size()).append("):\n");
        for (String skill : analysis.getCompetencesManquantes()) {
            details.append("  ✗ ").append(skill).append("\n");
        }
        
        detailsTextArea.setText(details.toString());
    }
    
    /**
     * Affiche le graphique des scores
     */
    private void showChart(List<CandidateAnalysis> analyses, List<Candidate> candidates) {
        if (chartContainer != null && scoresChart != null) {
            chartContainer.setManaged(true);
            chartContainer.setVisible(true);
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Scores");
            
            // Fix: Clear categories explicitly to avoid axis overlap/glitches
            CategoryAxis xAxis = (CategoryAxis) scoresChart.getXAxis();
            xAxis.getCategories().clear();
            
            // Optional: Disable animations for immediate refresh
            scoresChart.setAnimated(false);
            
            for (CandidateAnalysis analysis : analyses) {
                Candidate candidate = candidates.stream()
                    .filter(c -> c.getId() == analysis.getCandidateId())
                    .findFirst()
                    .orElse(null);
                
                if (candidate != null) {
                    String name = candidate.getPrenom() + " " + candidate.getNom();
                    series.getData().add(new XYChart.Data<>(name, analysis.getScore()));
                }
            }
            
            scoresChart.getData().clear();
            scoresChart.getData().add(series);
        }
    }
    
    /**
     * Sauvegarde les analyses dans la base de données
     */
    private void saveAnalysesToDatabase(List<CandidateAnalysis> analyses) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "INSERT INTO application (candidate_id, job_id, score, recommandation) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE score=VALUES(score), recommandation=VALUES(recommandation)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (CandidateAnalysis analysis : analyses) {
                    pstmt.setInt(1, analysis.getCandidateId());
                    pstmt.setInt(2, analysis.getJobId());
                    pstmt.setInt(3, analysis.getScore());
                    pstmt.setString(4, analysis.getRecommandation());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExportPDF() {
        if (filteredData.isEmpty()) {
            showAlert("Export", "Aucune donnée à exporter.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les résultats d'analyse");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        fileChooser.setInitialFileName("analyse_ia_export.csv");

        File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Candidat;Email;Score;Recommandation;Match");
                for (CandidateAnalysisRow row : filteredData) {
                    int detected = row.getAnalysis().getCompetencesDetectees().size();
                    int total = detected + row.getAnalysis().getCompetencesManquantes().size();
                    writer.printf("%s;%s;%d%%;%s;%d/%d%n",
                        row.getCandidateName(), row.getEmail(), row.getScore(), 
                        row.getRecommandation(), detected, total);
                }
                showAlert("Succès", "Résultats exportés vers : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'exportation : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void handleSendEmail() {
        showAlert("Email", "Fonctionnalité à implémenter (JavaMail API)", Alert.AlertType.INFORMATION);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Classe interne pour l'affichage dans la table
     */
    public static class CandidateAnalysisRow {
        private final String candidateName;
        private final String email;
        private final int score;
        private final String recommandation;
        private final CandidateAnalysis analysis;
        
        public CandidateAnalysisRow(String name, String email, int score, String rec, CandidateAnalysis analysis) {
            this.candidateName = name;
            this.email = email;
            this.score = score;
            this.recommandation = rec;
            this.analysis = analysis;
        }
        
        public String getCandidateName() { return candidateName; }
        public String getEmail() { return email; }
        public int getScore() { return score; }
        public String getRecommandation() { return recommandation; }
        public CandidateAnalysis getAnalysis() { return analysis; }
    }
}