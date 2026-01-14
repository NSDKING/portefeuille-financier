package fr.univ.projet.controller;

import fr.univ.projet.model.User;
import fr.univ.projet.service.DataStorage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        try {
 
            User utilisateurConnecte = DataStorage.loadUserSecurely(user, pass);
            fr.univ.projet.service.SessionManager.setCurrentUser(utilisateurConnecte);
            fr.univ.projet.service.SessionManager.setCurrentPassword(pass);
            chargerDashboard(utilisateurConnecte);
            
        } catch (Exception e) {
            errorLabel.setText("Identifiants incorrects ou accès refusé.");
            System.err.println("Échec connexion : " + e.getMessage());
        }
    }

    private void chargerDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/univ/projet/view/main-dashboard.fxml"));
            Parent root = loader.load();

            // Injection des données dans le MainController
            MainController mainController = loader.getController();
            mainController.setUserSession(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Aura Finance - " + user.getUsername());
            
        } catch (IOException e) {
            errorLabel.setText("Erreur lors du chargement de l'interface.");
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