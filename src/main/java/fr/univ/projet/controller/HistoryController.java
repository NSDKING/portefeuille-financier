package fr.univ.projet.controller;

import fr.univ.projet.model.Actif;
import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.Transaction;
import fr.univ.projet.service.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Collections;
import java.util.List;

public class HistoryController {

    @FXML private TableView<Transaction> historyTable;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colSymbol;
    @FXML private TableColumn<Transaction, Double> colQty;
    @FXML private TableColumn<Transaction, Double> colPrice;
    @FXML private TableColumn<Transaction, String> colTotal;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadTransactionData();
    }

    private void setupTableColumns() {
        // 1. Date
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));

        // 2. Type (Achat/Vente) avec couleur
        colType.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getQuantite() > 0 ? "ACHAT" : "VENTE"
        ));
        configureTypeColumnStyling();

        // 3. Symbole (Ticker)
        colSymbol.setCellValueFactory(cell -> {
            List<Actif> actifs = cell.getValue().getActifs();
            String ticker = (actifs != null && !actifs.isEmpty()) ? actifs.get(0).getTicker() : "N/A";
            return new SimpleStringProperty(ticker);
        });

        // 4. Quantité (Valeur absolue pour l'affichage)
        colQty.setCellValueFactory(cell -> new SimpleObjectProperty<>(Math.abs(cell.getValue().getQuantite())));

        // 5. Prix Unitaire
        colPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrixUnitaire()));

        // 6. Total calculé
        colTotal.setCellValueFactory(cell -> {
            double total = Math.abs(cell.getValue().getQuantite() * cell.getValue().getPrixUnitaire());
            return new SimpleStringProperty(String.format("%.2f €", total));
        });
    }

    private void configureTypeColumnStyling() {
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("ACHAT")) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadTransactionData() {
        if (SessionManager.getCurrentUser() != null && !SessionManager.getCurrentUser().getPortefeuilles().isEmpty()) {
            Portefeuille p = SessionManager.getCurrentUser().getPortefeuilles().get(0);
            
            // On crée une copie pour ne pas modifier la liste originale du modèle
            ObservableList<Transaction> transactions = FXCollections.observableArrayList(p.getTransactions());
            
            // Trier par date décroissante (la plus récente en haut)
            Collections.reverse(transactions);
            
            historyTable.setItems(transactions);
        }
    }
}