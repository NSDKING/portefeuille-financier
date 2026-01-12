package fr.univ.projet.controller;

import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.service.DataStorage;
import fr.univ.projet.service.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML

    private void handleRegister() {
        try {
            Portefeuille p = new Portefeuille(usernameField.getText(), "Mon Portefeuille", "EUR");
            p.setCash(0.0);

            DataStorage.saveNewUser(usernameField.getText(), passwordField.getText(), p);
            
            errorLabel.setText("Inscription réussie !");
        } catch (Exception e) {
            errorLabel.setText("Erreur : " + e.getMessage());
        }
    }
    @FXML
    private void handleBack() throws Exception {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fr/univ/projet/view/login.fxml"));
        stage.getScene().setRoot(root);
    }
}