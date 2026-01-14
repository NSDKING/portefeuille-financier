package fr.univ.projet.controller;

import fr.univ.projet.model.*;
import fr.univ.projet.service.AlphaVantageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AssetsController {

    @FXML private Label cashLabel, assetsValueLabel, previewLabel;
    @FXML private TableView<AssetRow> assetsTable;
    @FXML private TableColumn<AssetRow, String> colSymbol;
    @FXML private TableColumn<AssetRow, Number> colQty, colPrice, colValue;
    @FXML private ComboBox<String> cryptoSelector;
    @FXML private TextField amountField;

    private Portefeuille activePortfolio;
    private final ObservableList<AssetRow> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Liaison des colonnes
        colSymbol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        colQty.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        colPrice.setCellValueFactory(cellData -> cellData.getValue().currentPriceProperty());
        colValue.setCellValueFactory(cellData -> cellData.getValue().totalValueProperty());
        
        assetsTable.setItems(tableData);
        cryptoSelector.getItems().addAll("BTC", "ETH", "SOL", "ADA");

        // Listener pour l'aperçu du montant en temps réel
        amountField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    public void setPortfolio(Portefeuille portfolio) {
        this.activePortfolio = portfolio;
        refreshUI();
    }

    private void refreshUI() {
        if (activePortfolio == null) return;

        cashLabel.setText(String.format("%.2f €", activePortfolio.getCash()));
        tableData.clear();
        
        // 1. Map pour regrouper par TICKER : Clé = ticker, Valeur = Quantité cumulée
        java.util.Map<String, Double> synthese = new java.util.HashMap<>();

        // 2. Parcourir les transactions du portefeuille
        for (Transaction t : activePortfolio.getTransactions()) {
            for (Actif a : t.getActifs()) {
                String ticker = a.getTicker(); // On utilise le getter de ta classe abstraite
                double qteExistante = synthese.getOrDefault(ticker, 0.0);
                synthese.put(ticker, qteExistante + t.getQuantite());
            }
        }

        // 3. Récupérer les prix et remplir le tableau
        double totalAssetsValue = 0;
        for (java.util.Map.Entry<String, Double> entry : synthese.entrySet()) {
            String ticker = entry.getKey();
            double quantiteTotale = entry.getValue();

            // Récupération du prix frais via ton service
            double price = AlphaVantageService.getLatestPrice(ticker);
            
            // Si l'API échoue, on peut utiliser le prixActuel stocké dans l'objet par défaut
            if (price <= 0) {
                // Ici, on cherche l'actif dans la liste pour avoir son dernier prix connu
                // C'est une sécurité si l'API est hors ligne
                price = findLastKnownPrice(ticker); 
            }

            tableData.add(new AssetRow(ticker, quantiteTotale, price));
            totalAssetsValue += (quantiteTotale * price);
        }

        assetsValueLabel.setText(String.format("%.2f €", totalAssetsValue));
    }

    private double findLastKnownPrice(String ticker) {
        for (Transaction t : activePortfolio.getTransactions()) {
            for (Actif a : t.getActifs()) {
                if (a.getTicker().equals(ticker)) return a.getPrixActuel();
            }
        }
        return 0.0;
    }
    
    private void updatePreview() {
        try {
            double eur = Double.parseDouble(amountField.getText());
            String crypto = cryptoSelector.getValue();
            if (crypto != null) {
                // Simulation d'un prix (à remplacer par un appel API réel)
                double price = (crypto.equals("BTC")) ? 40000.0 : 2200.0;
                previewLabel.setText(String.format("Estimation : %.6f %s", eur / price, crypto));
            }
        } catch (NumberFormatException e) {
            previewLabel.setText("Estimation : 0.00 units");
        }
    }

    @FXML
    private void handleBuyAction() {
        try {
            double eurAmount = Double.parseDouble(amountField.getText());
            String symbol = cryptoSelector.getValue();

            if (activePortfolio.getCash() >= eurAmount && symbol != null) {
                // 1. Déduire Cash
                activePortfolio.setCash(activePortfolio.getCash() - eurAmount);
                
                // 2. Récupérer prix actuel
                double price = (symbol.equals("BTC")) ? 40000.0 : 2200.0;
                
                // 3. Ajouter transaction
                double qtyBought = eurAmount / price;
                double currentPrice = price;
                activePortfolio.addTransaction(new Transaction(qtyBought, currentPrice, symbol, currentPrice*qtyBought*0.01)); // Frais de 1%

                refreshUI();
                amountField.clear();
                System.out.println("Achat réussi !");
            } else {
                System.err.println("Fonds insuffisants ou sélection invalide.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'achat.");
        }
    }
}