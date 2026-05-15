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
import service.DatabaseService;
import utils.PDFExtractor;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import java.io.File;
import java.sql.*;
import java.util.Optional;

public class RecruitmentController {
    
    @FXML private TabPane tabPane;
    
    // Onglet Offres
    @FXML private TableView<Job> jobsTable;
    @FXML private TableColumn<Job, Integer> colJobId;
    @FXML private TableColumn<Job, String> colJobTitre;
    @FXML private TableColumn<Job, String> colJobDept;
    @FXML private TableColumn<Job, String> colJobCompetences;
    @FXML private TableColumn<Job, String> colJobStatut;
    @FXML private TableColumn<Job, Integer> colJobCandidats;
    @FXML private TableColumn<Job, Void> colJobActions;
    @FXML private ToggleGroup jobStatusGroup;
    
    // Onglet Candidats
    @FXML private TableView<Candidate> candidatesTable;
    @FXML private TableColumn<Candidate, Integer> colCandId;
    @FXML private TableColumn<Candidate, String> colCandNom;
    @FXML private TableColumn<Candidate, String> colCandPrenom;
    @FXML private TableColumn<Candidate, String> colCandEmail;
    @FXML private TableColumn<Candidate, String> colCandTel;
    @FXML private TableColumn<Candidate, String> colCandCV;
    @FXML private TableColumn<Candidate, Void> colCandActions;
    @FXML private TextField candidateSearchField;
    
    private ObservableList<Job> jobsList;
    private ObservableList<Candidate> candidatesList;
    
    @FXML
    public void initialize() {
        jobsList = FXCollections.observableArrayList();
        candidatesList = FXCollections.observableArrayList();
        
        setupJobsTable();
        setupCandidatesTable();
        
        loadJobs();
        loadCandidates();
        
        setupJobFilters();
        setupCandidateSearch();
    }
    
    /**
     * Configuration de la table des offres
     */
    private void setupJobsTable() {
        colJobId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colJobTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colJobDept.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colJobCompetences.setCellValueFactory(new PropertyValueFactory<>("competencesRequises"));
        colJobStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        // Colonne nombre de candidats (à calculer)
        colJobCandidats.setCellValueFactory(cellData -> {
            int jobId = cellData.getValue().getId();
            int count = getCandidateCountForJob(jobId);
            return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
        });
        
        // Style conditionnel pour le statut
        colJobStatut.setCellFactory(column -> new TableCell<Job, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Ouverte".equals(item)) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    }
                }
            }
        });
        
        setupJobActionsColumn();
        jobsTable.setItems(jobsList);
    }
    
    /**
     * Configuration des actions pour les offres
     */
    private void setupJobActionsColumn() {
        Callback<TableColumn<Job, Void>, TableCell<Job, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button btnEdit = new Button("✏️");
                private final Button btnClose = new Button("🔒");
                
                {
                    btnEdit.setOnAction(event -> {
                        Job job = getTableView().getItems().get(getIndex());
                        handleEditJob(job);
                    });
                    
                    btnClose.setOnAction(event -> {
                        Job job = getTableView().getItems().get(getIndex());
                        handleCloseJob(job);
                    });
                }
                
                private final HBox pane = new HBox(5, btnEdit, btnClose);
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            };
        };
        
        colJobActions.setCellFactory(cellFactory);
    }
    
    /**
     * Configuration de la table des candidats
     */
    private void setupCandidatesTable() {
        colCandId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCandPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCandEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCandTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        
        colCandCV.setCellValueFactory(cellData -> {
            String cvPath = cellData.getValue().getCvFilePath();
            String status = (cvPath != null && !cvPath.isEmpty()) ? "✓ Uploadé" : "✗ Manquant";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        
        setupCandidateActionsColumn();
        candidatesTable.setItems(candidatesList);
    }
    
    /**
     * Configuration des actions pour les candidats
     */
    private void setupCandidateActionsColumn() {
        Callback<TableColumn<Candidate, Void>, TableCell<Candidate, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button btnView = new Button("👁️");
                private final Button btnAddCV = new Button("📄");
                private final Button btnDelete = new Button("🗑️");
                
                {
                    btnView.setOnAction(event -> {
                        Candidate candidate = getTableView().getItems().get(getIndex());
                        handleViewCandidate(candidate);
                    });
                    
                    btnAddCV.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    btnAddCV.setOnAction(event -> {
                        Candidate candidate = getTableView().getItems().get(getIndex());
                        handleAddCVToCandidate(candidate);
                    });
                    
                    btnDelete.setStyle("-fx-background-color: #e74c3c;");
                    btnDelete.setOnAction(event -> {
                        Candidate candidate = getTableView().getItems().get(getIndex());
                        handleDeleteCandidate(candidate);
                    });
                }
                
                private final HBox pane = new HBox(5, btnView, btnAddCV, btnDelete);
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            };
        };
        
        colCandActions.setCellFactory(cellFactory);
    }
    
    /**
     * Charge les offres d'emploi
     */
    private void loadJobs() {
        jobsList.clear();
        
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM job ORDER BY id DESC";
            
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
                    
                    jobsList.add(job);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Charge les candidats
     */
    private void loadCandidates() {
        candidatesList.clear();
        
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM candidate ORDER BY id DESC";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Candidate candidate = new Candidate();
                    candidate.setId(rs.getInt("id"));
                    candidate.setNom(rs.getString("nom"));
                    candidate.setPrenom(rs.getString("prenom"));
                    candidate.setEmail(rs.getString("email"));
                    candidate.setTelephone(rs.getString("telephone"));
                    candidate.setCvText(rs.getString("cv_text"));
                    candidate.setCvFilePath(rs.getString("cv_file_path"));
                    
                    candidatesList.add(candidate);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Obtenir le nombre de candidats pour une offre
     */
    private int getCandidateCountForJob(int jobId) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT COUNT(*) FROM application WHERE job_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, jobId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Filtres pour les offres
     */
    private void setupJobFilters() {
        if (jobStatusGroup != null) {
            jobStatusGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    RadioButton rb = (RadioButton) newVal;
                    filterJobs(rb.getText());
                }
            });
        }
    }
    
    private void filterJobs(String filter) {
        if ("Tous".equals(filter)) {
            jobsTable.setItems(jobsList);
        } else {
            ObservableList<Job> filtered = jobsList.filtered(job -> filter.equals(job.getStatut()));
            jobsTable.setItems(filtered);
        }
    }
    
    /**
     * Recherche de candidats
     */
    private void setupCandidateSearch() {
        if (candidateSearchField != null) {
            candidateSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.isEmpty()) {
                    candidatesTable.setItems(candidatesList);
                } else {
                    String lowerCaseFilter = newVal.toLowerCase();
                    ObservableList<Candidate> filtered = candidatesList.filtered(cand -> 
                        cand.getNom().toLowerCase().contains(lowerCaseFilter) || 
                        cand.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                        cand.getEmail().toLowerCase().contains(lowerCaseFilter)
                    );
                    candidatesTable.setItems(filtered);
                }
            });
        }
    }
    
    /**
     * Ajouter une nouvelle offre
     */
    @FXML
    private void handleNewJob() {
        Dialog<Job> dialog = createJobDialog(null);
        
        Optional<Job> result = dialog.showAndWait();
        result.ifPresent(job -> {
            if (saveJob(job)) {
                loadJobs();
                showAlert("Succès", "Offre créée avec succès", Alert.AlertType.INFORMATION);
            }
        });
    }
    
    /**
     * Modifier une offre
     */
    private void handleEditJob(Job job) {
        Dialog<Job> dialog = createJobDialog(job);
        
        Optional<Job> result = dialog.showAndWait();
        result.ifPresent(updatedJob -> {
            if (updateJob(updatedJob)) {
                loadJobs();
                showAlert("Succès", "Offre modifiée", Alert.AlertType.INFORMATION);
            }
        });
    }
    
    /**
     * Fermer une offre
     */
    private void handleCloseJob(Job job) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Fermer l'offre");
        confirmation.setContentText("Voulez-vous fermer cette offre d'emploi ?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                job.setStatut("Fermée");
                updateJob(job);
                loadJobs();
            }
        });
    }
    
    /**
     * Créer le dialogue d'ajout/modification d'offre
     */
    private Dialog<Job> createJobDialog(Job job) {
        Dialog<Job> dialog = new Dialog<>();
        dialog.setTitle(job == null ? "Nouvelle Offre" : "Modifier Offre");
        
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField titreField = new TextField(job != null ? job.getTitre() : "");
        TextArea descField = new TextArea(job != null ? job.getDescription() : "");
        descField.setPrefRowCount(3);
        TextField compField = new TextField(job != null ? job.getCompetencesRequises() : "");
        TextField deptField = new TextField(job != null ? job.getDepartement() : "");
        
        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titreField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Compétences (séparées par ,):"), 0, 2);
        grid.add(compField, 1, 2);
        grid.add(new Label("Département:"), 0, 3);
        grid.add(deptField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Job newJob = job != null ? job : new Job();
                newJob.setTitre(titreField.getText());
                newJob.setDescription(descField.getText());
                newJob.setCompetencesRequises(compField.getText());
                newJob.setDepartement(deptField.getText());
                return newJob;
            }
            return null;
        });
        
        return dialog;
    }
    
    /**
     * Sauvegarder une offre
     */
    private boolean saveJob(Job job) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "INSERT INTO job (titre, description, competences_requises, departement, statut) " +
                        "VALUES (?, ?, ?, ?, 'Ouverte')";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, job.getTitre());
                pstmt.setString(2, job.getDescription());
                pstmt.setString(3, job.getCompetencesRequises());
                pstmt.setString(4, job.getDepartement());
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mettre à jour une offre
     */
    private boolean updateJob(Job job) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "UPDATE job SET titre=?, description=?, competences_requises=?, " +
                        "departement=?, statut=? WHERE id=?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, job.getTitre());
                pstmt.setString(2, job.getDescription());
                pstmt.setString(3, job.getCompetencesRequises());
                pstmt.setString(4, job.getDepartement());
                pstmt.setString(5, job.getStatut());
                pstmt.setInt(6, job.getId());
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ajouter un candidat
     */
    @FXML
    private void handleAddCandidate() {
        Dialog<Candidate> dialog = createCandidateDialog(null);
        
        Optional<Candidate> result = dialog.showAndWait();
        result.ifPresent(candidate -> {
            if (saveCandidate(candidate)) {
                loadCandidates();
                showAlert("Succès", "Candidat ajouté", Alert.AlertType.INFORMATION);
            }
        });
    }
    
    /**
     * Créer dialogue candidat
     */
    private Dialog<Candidate> createCandidateDialog(Candidate candidate) {
        Dialog<Candidate> dialog = new Dialog<>();
        dialog.setTitle(candidate == null || candidate.getId() == 0 ? "Nouveau Candidat" : "Modifier Candidat");
        
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // ✅ FIX: null-safe initialization to avoid NPE when fields are not yet set
        TextField nomField    = new TextField(candidate != null && candidate.getNom()       != null ? candidate.getNom()       : "");
        TextField prenomField = new TextField(candidate != null && candidate.getPrenom()    != null ? candidate.getPrenom()    : "");
        TextField emailField  = new TextField(candidate != null && candidate.getEmail()     != null ? candidate.getEmail()     : "");
        TextField telField    = new TextField(candidate != null && candidate.getTelephone() != null ? candidate.getTelephone() : "");
        
        grid.add(new Label("Nom *:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom *:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Téléphone:"), 0, 3);
        grid.add(telField, 1, 3);
        
        // Liste des offres pour postuler (uniquement les offres ouvertes)
        ComboBox<Job> jobComboBox = new ComboBox<>(jobsList.filtered(job -> "Ouverte".equals(job.getStatut())));
        jobComboBox.setPromptText("Sélectionner une offre (optionnel)");
        jobComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Si on édite un candidat, on pourrait essayer de présélectionner l'offre (optionnel ici)
        if (candidate != null && candidate.getJobApplication() != null) {
            jobComboBox.setValue(candidate.getJobApplication());
        }
        
        grid.add(new Label("Offre d'emploi:"), 0, 4);
        grid.add(jobComboBox, 1, 4);
        
        final String[] tempCvData = new String[2]; // [0] = path, [1] = text
        if (candidate != null) {
             tempCvData[0] = candidate.getCvFilePath();
             tempCvData[1] = candidate.getCvText();
        }
        
        Label cvLabel = new Label();
        if (tempCvData[0] != null && !tempCvData[0].isEmpty()) {
            cvLabel.setText("📄 " + new File(tempCvData[0]).getName());
            cvLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            cvLabel.setText("Aucun CV attaché");
            cvLabel.setStyle("-fx-text-fill: #7f8c8d;");
        }
        
        Button btnSelectCV = new Button("Sélectionner un CV");
        btnSelectCV.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner un CV (PDF)");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                try {
                    tempCvData[1] = PDFExtractor.extractAndCleanText(file.getAbsolutePath());
                    tempCvData[0] = file.getAbsolutePath();
                    cvLabel.setText("📄 " + file.getName());
                    cvLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        grid.add(new Label("CV:"), 0, 5);
        grid.add(new HBox(10, btnSelectCV, cvLabel), 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // ✅ FIX: validation obligatoire du nom et prénom
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        if (saveButton != null) {
            saveButton.setDisable(nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty());
            nomField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(newVal.trim().isEmpty() || prenomField.getText().trim().isEmpty()));
            prenomField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(newVal.trim().isEmpty() || nomField.getText().trim().isEmpty()));
        }
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Candidate cand = candidate != null ? candidate : new Candidate();
                cand.setNom(nomField.getText().trim());
                cand.setPrenom(prenomField.getText().trim());
                cand.setEmail(emailField.getText().trim());
                cand.setTelephone(telField.getText().trim());
                
                // Attacher le CV
                cand.setCvFilePath(tempCvData[0]);
                cand.setCvText(tempCvData[1]);
                
                // Attacher l'offre sélectionnée
                cand.setJobApplication(jobComboBox.getValue());
                
                return cand;
            }
            return null;
        });
        
        return dialog;
    }
    
    /**
     * Sauvegarder un candidat
     */
    private boolean saveCandidate(Candidate candidate) {
        try (Connection conn = DatabaseService.getConnection()) {
            boolean isNew = candidate.getId() == 0;
            String sql;
            
            if (isNew) {
                sql = "INSERT INTO candidate (nom, prenom, email, telephone, cv_text, cv_file_path) VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE candidate SET nom=?, prenom=?, email=?, telephone=?, cv_text=?, cv_file_path=? WHERE id=?";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, candidate.getNom());
                pstmt.setString(2, candidate.getPrenom());
                pstmt.setString(3, candidate.getEmail());
                pstmt.setString(4, candidate.getTelephone());
                pstmt.setString(5, candidate.getCvText() != null ? candidate.getCvText() : "");
                pstmt.setString(6, candidate.getCvFilePath());
                
                if (!isNew) {
                    pstmt.setInt(7, candidate.getId());
                }
                
                pstmt.executeUpdate();
                
                if (isNew) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            candidate.setId(generatedKeys.getInt(1));
                        }
                    }
                }
                
                // Gérer l'application (liaison avec l'offre)
                if (candidate.getId() > 0 && candidate.getJobApplication() != null) {
                    // Utiliser INSERT IGNORE ou ON DUPLICATE KEY pour éviter les erreurs si déjà postulé
                    String sqlApp = "INSERT INTO application (candidate_id, job_id, date_candidature) " +
                                   "VALUES (?, ?, CURRENT_DATE) " +
                                   "ON DUPLICATE KEY UPDATE date_candidature=CURRENT_DATE";
                    try (PreparedStatement pstmtApp = conn.prepareStatement(sqlApp)) {
                        pstmtApp.setInt(1, candidate.getId());
                        pstmtApp.setInt(2, candidate.getJobApplication().getId());
                        pstmtApp.executeUpdate();
                    }
                }
                
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Importer un CV PDF
     */
    @FXML
    private void handleImportCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un CV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        
        File file = fileChooser.showOpenDialog(candidatesTable.getScene().getWindow());
        
        if (file != null) {
            try {
                String cvText = PDFExtractor.extractAndCleanText(file.getAbsolutePath());
                
                // Créer un candidat avec le CV
                Candidate candidate = new Candidate();
                candidate.setCvText(cvText);
                candidate.setCvFilePath(file.getAbsolutePath());
                
                // Dialogue pour compléter les infos
                Dialog<Candidate> dialog = createCandidateDialog(candidate);
                Optional<Candidate> result = dialog.showAndWait();
                result.ifPresent(cand -> {
                    if (saveCandidate(cand)) {
                        loadCandidates();
                        showAlert("Succès",
                            "CV importé avec succès pour : " + cand.getPrenom() + " " + cand.getNom(),
                            Alert.AlertType.INFORMATION);
                    } else {
                        // ✅ FIX: feedback explicite en cas d'échec de sauvegarde
                        showAlert("Erreur",
                            "Impossible de sauvegarder le candidat.\n"
                            + "Vérifiez que le nom et le prénom sont bien renseignés.",
                            Alert.AlertType.ERROR);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'import du CV", Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Voir les détails d'un candidat
     */
    private void handleViewCandidate(Candidate candidate) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Candidat");
        alert.setHeaderText(candidate.getPrenom() + " " + candidate.getNom());
        
        TextArea textArea = new TextArea();
        textArea.setText("Email: " + candidate.getEmail() + "\n" +
                        "Téléphone: " + candidate.getTelephone() + "\n\n" +
                        "CV:\n" + candidate.getCvText());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
    
    /**
     * Ajouter/Modifier le CV d'un candidat existant
     */
    private void handleAddCVToCandidate(Candidate candidate) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un CV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        
        File file = fileChooser.showOpenDialog(candidatesTable.getScene().getWindow());
        
        if (file != null) {
            try {
                String cvText = PDFExtractor.extractAndCleanText(file.getAbsolutePath());
                candidate.setCvText(cvText);
                candidate.setCvFilePath(file.getAbsolutePath());
                
                if (updateCandidateCV(candidate)) {
                    loadCandidates();
                    showAlert("Succès", "CV ajouté avec succès pour : " + candidate.getPrenom() + " " + candidate.getNom(), Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Erreur lors de la mise à jour du CV en base de données.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'import du CV", Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Mettre à jour le CV d'un candidat en base de données
     */
    private boolean updateCandidateCV(Candidate candidate) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "UPDATE candidate SET cv_text = ?, cv_file_path = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, candidate.getCvText() != null ? candidate.getCvText() : "");
                pstmt.setString(2, candidate.getCvFilePath() != null ? candidate.getCvFilePath() : "");
                pstmt.setInt(3, candidate.getId());
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprimer un candidat
     */
    private void handleDeleteCandidate(Candidate candidate) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Supprimer candidat");
        confirmation.setContentText("Supprimer " + candidate.getPrenom() + " " + candidate.getNom() + " ?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseService.getConnection()) {
                    String sql = "DELETE FROM candidate WHERE id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, candidate.getId());
                        pstmt.executeUpdate();
                        loadCandidates();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Afficher une alerte
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}