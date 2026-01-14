package fr.univ.projet.controller;

import fr.univ.projet.model.*;
import fr.univ.projet.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HistoryController {

    @FXML private TableView<TransactionRow> historyTable;
    @FXML private TableColumn<TransactionRow, String> colType, colDate, colSymbol;
    @FXML private TableColumn<TransactionRow, Number> colQty, colPrice, colFees, colTotal;

    private final ObservableList<TransactionRow> historyData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Liaison des colonnes
        colType.setCellValueFactory(d -> d.getValue().typeProperty());
        colDate.setCellValueFactory(d -> d.getValue().dateProperty());
        colSymbol.setCellValueFactory(d -> d.getValue().symbolProperty());
        colQty.setCellValueFactory(d -> d.getValue().quantityProperty());
        colPrice.setCellValueFactory(d -> d.getValue().priceProperty());
        colFees.setCellValueFactory(d -> d.getValue().feesProperty());
        colTotal.setCellValueFactory(d -> d.getValue().totalProperty());

        historyTable.setItems(historyData);
        loadHistory();
    }

    private void loadHistory() {
        User user = SessionManager.getCurrentUser();
        if (user == null || user.getPortefeuilles().isEmpty()) return;

        historyData.clear();
        Portefeuille p = user.getPortefeuilles().get(0); // Portefeuille principal

        for (Transaction t : p.getTransactions()) {
            // On récupère le ticker de l'actif lié
            String symbol = t.getActifs().isEmpty() ? "Inconnu" : t.getActifs().get(0).getTicker();
            
            // Déterminer si c'est un achat ou une vente selon la quantité
            String type = t.getQuantite() > 0 ? "ACHAT" : "VENTE";
            
            historyData.add(new TransactionRow(
                type,
                t.getDate(),
                symbol,
                Math.abs(t.getQuantite()), // On affiche la quantité en valeur absolue
                t.getPrixUnitaire(),
                t.getFrais()
            ));
        }
        
        // Trier par date (la plus récente en haut)
        historyData.sort((a, b) -> b.dateProperty().get().compareTo(a.dateProperty().get()));
    }
}