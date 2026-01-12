package fr.univ.projet.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        // LOGIQUE DE SÉCURITÉ :
        // 1. Vérifier si l'utilisateur existe
        // 2. Tenter de déchiffrer le fichier avec 'pass'
        if (validerConnexion(user, pass)) {
            chargerDashboard();
        } else {
            errorLabel.setText("Identifiants incorrects ou clé invalide.");
        }
    }

    private boolean validerConnexion(String user, String pass) {
        // Pour l'instant, on simule. Plus tard, on utilisera une classe SecurityService
        return "admin".equals(user) && "1234".equals(pass);
    }

    private void chargerDashboard() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fr/univ/projet/view/main-dashboard.fxml"));
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/fr/univ/projet/view/register.fxml"));
                stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}