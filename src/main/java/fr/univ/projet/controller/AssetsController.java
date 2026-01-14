package fr.univ.projet.controller;

import fr.univ.projet.model.*;
import fr.univ.projet.service.AlphaVantageService;
import fr.univ.projet.service.DataStorage;
import fr.univ.projet.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.Map;

public class AssetsController {

    @FXML private Label cashLabel, assetsValueLabel, previewLabel;
    @FXML private TableView<AssetRow> assetsTable;
    @FXML private TableColumn<AssetRow, String> colSymbol;
    @FXML private TableColumn<AssetRow, Number> colQty, colPrice, colValue;
    
    // Éléments Achat
    @FXML private ComboBox<String> tickerSelector; 
    @FXML private TextField amountField;

    // Éléments Vente (Nouveaux)
    @FXML private ComboBox<String> ownedAssetsSelector;
    @FXML private TextField sellQtyField;

    private Portefeuille activePortfolio;
    private final ObservableList<AssetRow> tableData = FXCollections.observableArrayList();
    private final Map<String, Double> currentQuantities = new HashMap<>();

    @FXML
    public void initialize() {
        // Liaison des colonnes du tableau
        colSymbol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        colQty.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        colPrice.setCellValueFactory(cellData -> cellData.getValue().currentPriceProperty());
        colValue.setCellValueFactory(cellData -> cellData.getValue().totalValueProperty());
        
        assetsTable.setItems(tableData);
        
        // Liste des tickers disponibles à l'achat (Actions et Cryptos)
        tickerSelector.getItems().addAll("BTC", "ETH", "AAPL", "TSLA", "MSFT", "GOOGL");

        // Listeners pour mise à jour de l'aperçu prix/quantité
        amountField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        tickerSelector.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    public void setPortfolio(Portefeuille portfolio) {
        this.activePortfolio = portfolio;
        refreshUI();
    }

    private void refreshUI() {
        if (activePortfolio == null) return;

        cashLabel.setText(String.format("%.2f €", activePortfolio.getCash()));
        tableData.clear();
        currentQuantities.clear();
        ownedAssetsSelector.getItems().clear();

        for (Transaction t : activePortfolio.getTransactions()) {
            for (Actif a : t.getActifs()) {
                String ticker = a.getTicker(); // Utilisation de getTicker() de la classe Actif
                
                double qteExistante = currentQuantities.getOrDefault(ticker, 0.0);
                
                currentQuantities.put(ticker, qteExistante + t.getQuantite());
            }
        }
                

        // 2. Remplissage du tableau et du sélecteur de vente
        double totalAssetsValue = 0;
        for (Map.Entry<String, Double> entry : currentQuantities.entrySet()) {
            if (entry.getValue() > 0.000001) { // On n'affiche que si la quantité est positive
                String ticker = entry.getKey();
                double qty = entry.getValue();
                
                double price = AlphaVantageService.getLatestPrice(ticker);
                if (price <= 0) price = findLastKnownPrice(ticker);

                tableData.add(new AssetRow(ticker, qty, price));
                ownedAssetsSelector.getItems().add(ticker);
                totalAssetsValue += (qty * price);
            }
        }

        assetsValueLabel.setText(String.format("%.2f €", totalAssetsValue));
    }

    @FXML
    private void handleBuyAction() {
        try {
            double eurAmount = Double.parseDouble(amountField.getText());
            String symbol = tickerSelector.getValue();

            if (symbol != null && activePortfolio.getCash() >= eurAmount) {
                double price = AlphaVantageService.getLatestPrice(symbol);
                if (price <= 0) throw new Exception("Impossible de récupérer le prix.");

                double qtyToBuy = eurAmount / price;
                
                // Déduction du cash et ajout transaction
                activePortfolio.setCash(activePortfolio.getCash() - eurAmount);
                activePortfolio.addTransaction(new Transaction(qtyToBuy, price, symbol, eurAmount * 0.001)); 
                DataStorage.saveUser(SessionManager.getCurrentUser(), SessionManager.getCurrentPassword());
                 amountField.clear();
                refreshUI();
            }
        } catch (Exception e) {
            showError("Erreur d'achat", "Vérifiez le montant et la connexion API.");
        }
    }

    @FXML
    private void handleSellAction() {
        try {
            String symbol = ownedAssetsSelector.getValue();
            double qtyToSell = Double.parseDouble(sellQtyField.getText());
            double currentOwned = currentQuantities.getOrDefault(symbol, 0.0);

            if (symbol != null && qtyToSell <= currentOwned && qtyToSell > 0) {
                double price = AlphaVantageService.getLatestPrice(symbol);
                double gain = qtyToSell * price;

                // Ajout du cash et ajout transaction négative (vente)
                activePortfolio.setCash(activePortfolio.getCash() + gain);
                activePortfolio.addTransaction(new Transaction(-qtyToSell, price, symbol, gain * 0.001));

                sellQtyField.clear();
                refreshUI();
            } else {
                showError("Vente impossible", "Quantité insuffisante ou invalide.");
            }
        } catch (Exception e) {
            showError("Erreur de vente", "Vérifiez la quantité saisie.");
        }
    }

    private void updatePreview() {
        try {
            String ticker = tickerSelector.getValue();
            String input = amountField.getText();
            if (ticker != null && !input.isEmpty()) {
                double eur = Double.parseDouble(input);
                double price = AlphaVantageService.getLatestPrice(ticker);
                if (price > 0) {
                    previewLabel.setText(String.format("Estimation : %.6f %s", eur / price, ticker));
                }
            }
        } catch (Exception e) {
            previewLabel.setText("Estimation : 0.00 units");
        }
    }

    private double findLastKnownPrice(String ticker) {
        return activePortfolio.getTransactions().stream()
                // On cherche dans la liste d'actifs de chaque transaction
                .filter(t -> t.getActifs().stream()
                        .anyMatch(a -> a.getTicker().equals(ticker)))
                .map(Transaction::getPrixUnitaire)
                .findFirst()
                .orElse(0.0);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}