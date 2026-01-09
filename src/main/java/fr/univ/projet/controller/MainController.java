package fr.univ.projet.controller;

import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.service.DataStorage;
import fr.univ.projet.service.AnalyseurPerformance;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    // Les éléments de l'interface de Salom
    @FXML private Label totalLabel;
    @FXML private Label plusValueLabel;

    private Portefeuille monPortefeuille;

    @FXML
    public void initialize() {
        // Au démarrage, on charge les données via le service de David
        monPortefeuille = DataStorage.loadPortfolio("monPortefeuille");
        rafraichirVue();
    }

    @FXML
    private void handleRefresh() {
        // Appeler les calculs de Dimitri pour mettre à jour l'affichage
        rafraichirVue();
    }

    private void rafraichirVue() {
        double total = monPortefeuille.calculerValeurTotale();
        double pv = AnalyseurPerformance.calculerPlusValueTotale(monPortefeuille);
        
        totalLabel.setText(String.format("%.2f €", total));
        plusValueLabel.setText(String.format("%.2f €", pv));
    }
}