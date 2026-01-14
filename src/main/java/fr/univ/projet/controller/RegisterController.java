package fr.univ.projet.controller;

import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.User;
import fr.univ.projet.service.DataStorage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    // Éléments d'identification
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    
    // Éléments de configuration du portefeuille (Nouveaux)
    @FXML private TextField initialBalanceField;
    @FXML private ComboBox<String> currencyCombo;
    @FXML private CheckBox checkCourant, checkEpargne, checkActions, checkCrypto;
    
    
    @FXML private Label errorLabel;

    /**
     * Initialise les listes déroulantes au chargement de la vue
     */
    @FXML
    public void initialize() {
        // Initialisation des devises
        currencyCombo.getItems().addAll("EUR", "USD", "BTC", "ETH");
        currencyCombo.setValue("EUR");

     }

    /**
     * Logique de création de compte
     */
    @FXML
    private void handleRegister() {
        try {
            String user = usernameField.getText().trim(); // .trim() pour éviter les espaces inutiles
            String pass = passwordField.getText();

            // --- NOUVELLE VÉRIFICATION ---
            if (user.isEmpty()) {
                errorLabel.setText("Le nom d'utilisateur ne peut pas être vide.");
                return;
            }

            if (DataStorage.userExists(user)) {
                errorLabel.setText("Erreur : Le nom d'utilisateur '" + user + "' est déjà pris.");
                return;
            }
            // ------------------------------

            String currency = currencyCombo.getValue();
            double balance = 0.0;
            try {
                balance = Double.parseDouble(initialBalanceField.getText());
            } catch (NumberFormatException e) {
                errorLabel.setText("Veuillez entrer un solde initial valide.");
                return;
            }

            // 1. Créer l'utilisateur
            User nouveauUser = new User(user);

            // 2. Ajouter les portefeuilles (ton code actuel)
            nouveauUser.getPortefeuilles().add(new Portefeuille(user, "Compte Courant", currency));
            nouveauUser.getPortefeuilles().get(0).setCash(balance);

            if (checkEpargne.isSelected()) 
                nouveauUser.getPortefeuilles().add(new Portefeuille(user, "Compte Épargne", currency));
            if (checkActions.isSelected()) 
                nouveauUser.getPortefeuilles().add(new Portefeuille(user, "Compte Actions", currency));
            if (checkCrypto.isSelected()) 
                nouveauUser.getPortefeuilles().add(new Portefeuille(user, "Compte Crypto", currency));

            // 3. Sauvegarde
            DataStorage.saveNewUser(user, pass, nouveauUser);

            errorLabel.setText("Compte créé avec succès !");
            handleBack();

        } catch (Exception e) {
            errorLabel.setText("Erreur : " + e.getMessage());
        }
    }
    /**
     * Retourne à la page de connexion
     */
    @FXML
    private void handleBack() throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fr/univ/projet/view/login.fxml"));
        stage.getScene().setRoot(root);
    }
}