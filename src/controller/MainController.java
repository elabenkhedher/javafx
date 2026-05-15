/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author elabe
 */

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.application.Platform;

public class MainController {
    
    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnEmployees;
    @FXML private Button btnRecruitment;
    @FXML private Button btnAnalysis;
    
    @FXML
    public void initialize() {
        // Charger le dashboard au démarrage
        Platform.runLater(() -> showDashboard());
    }
    
    @FXML
    private void showDashboard() {
        loadView("/ressources/view/dashboard.fxml");
        setActiveButton(btnDashboard);
    }
    
    @FXML
    private void showEmployees() {
        loadView("/ressources/view/employees.fxml");
        setActiveButton(btnEmployees);
    }
    
    @FXML
    private void showRecruitment() {
        loadView("/ressources/view/recruitment.fxml");
        setActiveButton(btnRecruitment);
    }
    
    @FXML
    private void showAnalysis() {
        loadView("/ressources/view/analysis.fxml");
        setActiveButton(btnAnalysis);
    }
    
    /**
     * Charge une vue FXML dans la zone de contenu
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Définit le bouton actif dans la navigation
     */
    private void setActiveButton(Button activeBtn) {
        // Retirer la classe active de tous les boutons
        btnDashboard.getStyleClass().remove("nav-btn-active");
        btnEmployees.getStyleClass().remove("nav-btn-active");
        btnRecruitment.getStyleClass().remove("nav-btn-active");
        btnAnalysis.getStyleClass().remove("nav-btn-active");
        
        // Ajouter la classe active au bouton sélectionné
        if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }
    
    @FXML
    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // TODO: Implémenter la logique de déconnexion
                Platform.exit();
            }
        });
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}