package fr.univ.projet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override

    public void start(Stage stage) throws Exception {
    // 1. Charger le FXML
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fr/univ/projet/view/login.fxml"));
    Parent root = fxmlLoader.load();

    // 2. Créer la scène
    Scene scene = new Scene(root, 1024, 768);
 

    stage.setTitle("Aura Finance - Connexion");
    stage.setScene(scene);
    stage.show();
}

    public static void main(String[] args) {
        launch(args);
    }
}