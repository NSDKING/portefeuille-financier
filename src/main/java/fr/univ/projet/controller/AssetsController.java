package fr.univ.projet.controller;

import fr.univ.projet.model.*;
import fr.univ.projet.service.AlphaVantageService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AssetsController {
    @FXML private Label cashLabel, stocksValueLabel, cryptoValueLabel;
    @FXML private TableView<Actif> assetsTable;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> cryptoSelector;

    private User currentUser;
    private Portefeuille activePortfolio;

    public void setSession(User user, Portefeuille portfolio) {
        this.currentUser = user;
        this.activePortfolio = portfolio;
        initUI();
    }

    private void initUI() {
        cryptoSelector.getItems().addAll("BTC", "ETH", "SOL", "BNB");
        refreshValues();
    }

    private void refreshValues() {
        cashLabel.setText(String.format("%.2f €", activePortfolio.getCash()));
        // Logique de calcul pour les labels et le tableau...
    }

    @FXML
    private void handleBuyCrypto() {
        String symbol = cryptoSelector.getValue();
        String amountStr = amountField.getText();

        if (symbol == null || amountStr.isEmpty()) return;

        double eurAmount = Double.parseDouble(amountStr);
        
        // 1. Vérifier le solde de Cash
        if (activePortfolio.getCash() >= eurAmount) {
            // 2. Récupérer le prix actuel (via AlphaVantageService adapté aux cryptos)
            double currentPrice = 60000.0; // Exemple fixe pour test

            // 3. Déduire le cash et ajouter l'actif
            activePortfolio.setCash(activePortfolio.getCash() - eurAmount);
            double qtyBought = eurAmount / currentPrice;
            
            // Ajouter à la liste des transactions
            activePortfolio.addTransaction(new Transaction(qtyBought, currentPrice, symbol, currentPrice*qtyBought*0.01)); // Frais de 1%
            
            refreshValues();
            showAlert("Succès", "Achat de " + qtyBought + " " + symbol + " effectué.");
        } else {
            showAlert("Erreur", "Solde cash insuffisant.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}