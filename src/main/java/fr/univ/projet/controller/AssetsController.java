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

    @FXML private Label cashLabel, assetsValueLabel, previewLabel, sellPreviewLabel;
    @FXML private TableView<AssetRow> assetsTable;
    @FXML private TableColumn<AssetRow, String> colSymbol;
    @FXML private TableColumn<AssetRow, Number> colQty, colPrice, colValue;
    
    @FXML private ComboBox<String> tickerSelector, ownedAssetsSelector; 
    @FXML private TextField amountField, sellQtyField;

    private Portefeuille activePortfolio;
    private final ObservableList<AssetRow> tableData = FXCollections.observableArrayList();
    private final Map<String, Double> currentQuantities = new HashMap<>();
    private final Map<String, Double> priceCache = new HashMap<>();

    @FXML
    public void initialize() {
        // Configuration TableView
        colSymbol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        colQty.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        colPrice.setCellValueFactory(cellData -> cellData.getValue().currentPriceProperty());
        colValue.setCellValueFactory(cellData -> cellData.getValue().totalValueProperty());
        assetsTable.setItems(tableData);

        // Liste des actions avec prix par défaut (Cache de secours)
        Map<String, Double> defaultPrices = Map.ofEntries(
            Map.entry("AAPL", 215.30), Map.entry("MSFT", 410.15),
            Map.entry("GOOGL", 165.40), Map.entry("AMZN", 182.90),
            Map.entry("TSLA", 245.80), Map.entry("NVDA", 680.25),
            Map.entry("META", 485.60), Map.entry("NFLX", 610.10),
            Map.entry("ASML", 895.40), Map.entry("MC.PA", 740.00),
            Map.entry("V", 275.20), Map.entry("OR.PA", 425.15)
        );
        priceCache.putAll(defaultPrices);
        tickerSelector.getItems().addAll(priceCache.keySet().stream().sorted().toList());

        // Listeners pour les prévisualisations
        amountField.textProperty().addListener((obs, old, nv) -> updatePreview());
        tickerSelector.valueProperty().addListener((obs, old, nv) -> updatePreview());
        sellQtyField.textProperty().addListener((obs, old, nv) -> updateSellPreview());
        ownedAssetsSelector.valueProperty().addListener((obs, old, nv) -> updateSellPreview());
    }

    public void setPortfolio(Portefeuille portfolio) {
        this.activePortfolio = portfolio;
        refreshUI();
    }

    private double getRobustPrice(String ticker) {
        double price = AlphaVantageService.getLatestPrice(ticker);
        if (price > 0) {
            priceCache.put(ticker, price);
            return price;
        }
        return priceCache.getOrDefault(ticker, findLastKnownPrice(ticker));
    }

    private void refreshUI() {
        if (activePortfolio == null) return;

        cashLabel.setText(String.format("%.2f €", activePortfolio.getCash()));
        tableData.clear();
        currentQuantities.clear();
        ownedAssetsSelector.getItems().clear();

        // Analyse des transactions pour calculer les positions
        for (Transaction t : activePortfolio.getTransactions()) {
            for (Actif a : t.getActifs()) {
                String ticker = a.getTicker();
                currentQuantities.put(ticker, currentQuantities.getOrDefault(ticker, 0.0) + t.getQuantite());
            }
        }

        double totalAssetsValue = 0;
        for (Map.Entry<String, Double> entry : currentQuantities.entrySet()) {
            double qty = entry.getValue();
            if (qty > 0.000001) {
                String ticker = entry.getKey();
                double price = getRobustPrice(ticker);
                tableData.add(new AssetRow(ticker, qty, price));
                ownedAssetsSelector.getItems().add(ticker);
                totalAssetsValue += (qty * price);
            }
        }
        assetsValueLabel.setText(String.format("%.2f €", totalAssetsValue));
        assetsTable.refresh();
    }

    @FXML
    private void handleBuyAction() {
        try {
            User user = SessionManager.getCurrentUser();
            if (user == null) throw new Exception("Session expirée.");

            double eurAmount = Double.parseDouble(amountField.getText());
            String symbol = tickerSelector.getValue();

            if (symbol != null && activePortfolio.getCash() >= eurAmount) {
                double price = getRobustPrice(symbol);
                double qty = eurAmount / price;

                // Création et liaison de l'actif
                Transaction t = new Transaction(price, qty, java.time.LocalDate.now().toString(), eurAmount * 0.001);
                t.ajouterActif(new Action(symbol, symbol, price, "EUR", "Entreprise " + symbol, "Nasdaq", price));
                activePortfolio.setCash(activePortfolio.getCash() - eurAmount);
                activePortfolio.addTransaction(t);

                DataStorage.saveUser(user, SessionManager.getCurrentPassword());
                amountField.clear();
                refreshUI();
            }
        } catch (Exception e) {
            showError("Erreur d'achat", e.getMessage());
        }
    }

    @FXML
    private void handleSellAction() {
        try {
            User user = SessionManager.getCurrentUser();
            String symbol = ownedAssetsSelector.getValue();
            double qtyToSell = Double.parseDouble(sellQtyField.getText());
            double owned = currentQuantities.getOrDefault(symbol, 0.0);

            if (symbol != null && qtyToSell <= owned && qtyToSell > 0) {
                double price = getRobustPrice(symbol);
                double gain = qtyToSell * price;

                Transaction t = new Transaction(price, -qtyToSell, java.time.LocalDate.now().toString(), gain * 0.001);
                t.ajouterActif(new Action(symbol, symbol, price, "EUR", "Entreprise " + symbol, "Nasdaq", price));

                activePortfolio.setCash(activePortfolio.getCash() + gain);
                activePortfolio.addTransaction(t);

                DataStorage.saveUser(user, SessionManager.getCurrentPassword());
                sellQtyField.clear();
                refreshUI();
            }
        } catch (Exception e) {
            showError("Erreur de vente", "Vérifiez vos données.");
        }
    }

    private void updatePreview() {
        try {
            String ticker = tickerSelector.getValue();
            double eur = Double.parseDouble(amountField.getText());
            double price = getRobustPrice(ticker);
            if (price > 0) previewLabel.setText(String.format("Estimation : %.4f units", eur / price));
        } catch (Exception e) { previewLabel.setText(""); }
    }

    private void updateSellPreview() {
        try {
            String ticker = ownedAssetsSelector.getValue();
            double qty = Double.parseDouble(sellQtyField.getText());
            double price = getRobustPrice(ticker);
            if (price > 0) {
                sellPreviewLabel.setText(String.format("Valeur : %.2f €", qty * price));
                sellPreviewLabel.setStyle(qty > currentQuantities.get(ticker) ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
            }
        } catch (Exception e) { sellPreviewLabel.setText(""); }
    }

    private double findLastKnownPrice(String ticker) {
        return activePortfolio.getTransactions().stream()
                .filter(t -> t.getActifs().stream().anyMatch(a -> a.getTicker().equals(ticker)))
                .map(Transaction::getPrixUnitaire).findFirst().orElse(0.0);
    }

    private void showError(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setContentText(content); a.showAndWait();
    }
}