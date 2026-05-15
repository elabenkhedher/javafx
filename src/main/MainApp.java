package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.DatabaseService;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialiser la connexion MySQL (XAMPP)
        try {
            DatabaseService.initialize();
            System.out.println("Base de données initialisée avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }

        // Charger la vue principale
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/ressources/view/main.fxml")
        );
        Scene scene = new Scene(loader.load(), 1200, 750);
        scene.getStylesheets().add(
            getClass().getResource("/ressources/css/style.css").toExternalForm()
        );
        stage.setTitle("Système RH Intelligent");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Connexions gérées par appel — rien à fermer globalement
        DatabaseService.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
