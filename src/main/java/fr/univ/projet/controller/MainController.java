package fr.univ.projet.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.Transaction;
import fr.univ.projet.model.Event;
import fr.univ.projet.service.DataStorage;
import fr.univ.projet.service.AnalyseurPerformance;

import java.util.Map;
import java.util.stream.Collectors;

public class MainController {

    // --- Éléments FXML ---
    @FXML private Label totalValueLabel;
    @FXML private Label profitLabel;
    @FXML private Label profitPercentageLabel;
    @FXML private Label cashLabel;

    @FXML private LineChart<String, Number> performanceChart;
    @FXML private PieChart allocationChart;
    @FXML private CategoryAxis xAxis;

    @FXML private Button btnRefresh;
    @FXML private BorderPane rootPane;

    private Portefeuille monPortefeuille;

    /**
     * Méthode d'initialisation appelée par JavaFX après le chargement du FXML
     */
    @FXML
    public void initialize() {
        System.out.println("Initialisation du Dashboard...");
        chargerDonnees();

        if (btnRefresh != null) {
            btnRefresh.setOnAction(e -> chargerDonnees());
        }
    }

    /**
     * Charge le portefeuille depuis le stockage local (Backend David)
     */
    
    private void chargerDonnees() {
        // Tentative de chargement du fichier "mon_portefeuille.json"
        this.monPortefeuille = DataStorage.loadUserSecurely("mon_portefeuille");

        if (monPortefeuille != null) {
            rafraichirInterface();
        } else {
            System.err.println("Erreur : Impossible de charger le portefeuille.");
            initialiserVide();
        }
    }

    /**
     * Met à jour tous les composants visuels avec les données réelles
     */
    private void rafraichirInterface() {
        // 1. Calculs via AnalyseurPerformance (Backend Dimitri)
        double totalValeur = monPortefeuille.calculerValeurTotale();
        double profitTotal = AnalyseurPerformance.calculerPlusValueTotale(monPortefeuille);
        double cash = monPortefeuille.getCash();

        // Calcul du pourcentage de profit
        double investissementInitial = totalValeur - profitTotal;
        double pourcentage = (investissementInitial > 0) ? (profitTotal / investissementInitial) * 100 : 0;

        // 2. Mise à jour des Labels de résumé
        String devise = monPortefeuille.getMonnaieReference();
        totalValueLabel.setText(String.format("%.2f %s", totalValeur, devise));
        cashLabel.setText(String.format("%.2f %s", cash, devise));
        profitLabel.setText(String.format("%+.2f %s", profitTotal, devise));
        profitPercentageLabel.setText(String.format("%+.2f%%", pourcentage));

        // Style dynamique (Vert si positif, Rouge si négatif)
        appliquerStyleProfit(profitTotal);

        // 3. Mise à jour des Graphiques
        majLineChart();
        majPieChart();

        // 4. Analyse avancée (Console pour l'instant)
        executerAnalyseAvancee();
    }

    /**
     * Gère l'affichage de l'évolution chronologique et des événements
     */
    private void majLineChart() {
        performanceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valeur du Portefeuille (" + monPortefeuille.getMonnaieReference() + ")");

        Map<String, Double> historique = monPortefeuille.getHistoriqueValeurs();

        historique.forEach((date, valeur) -> {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(date, valeur);
            series.getData().add(dataPoint);

            // Vérifier si un événement (Krach, Hack, etc.) a eu lieu à cette date
            for (Event event : monPortefeuille.getEvents()) {
                if (event.getDate().equals(date)) {
                    // On ajoute un marqueur visuel et un Tooltip sur le point
                    dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            String info = String.format("ÉVÉNEMENT : %s\nImpact: %s (%.0f%%)\nType: %s", 
                                event.getLabel(), event.getImpact(), event.getImpactEstime() * 100, event.getType());
                            
                            Tooltip.install(newNode, new Tooltip(info));
                             newNode.setStyle("-fx-background-color: #e11d48, white; -fx-background-radius: 5px; -fx-cursor: hand;");
                        }
                    });
                }
            }
        });

        performanceChart.getData().add(series);
    }

    /**
     * Gère la répartition des actifs (ex: 60% Actions, 40% Crypto)
     */
    private void majPieChart() {
        allocationChart.getData().clear();

        // On groupe les transactions par catégorie pour l'analyse de répartition
        // Note : Assurez-vous que votre classe Transaction a une méthode getCategorie() ou getType()
        Map<String, Double> repartition = monPortefeuille.getTransactions().stream()
                .collect(Collectors.groupingBy(
                        t -> t.getFrais(), // Ici j'utilise 'frais' ou un autre champ en attendant un champ 'categorie'
                        Collectors.summingDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                ));

        repartition.forEach((label, valeur) -> {
            allocationChart.getData().add(new PieChart.Data(label, valeur));
        });
    }

    /**
     * Analyse si le portefeuille est plus souvent en profit ou en déficit
     */
    private void executerAnalyseAvancee() {
        Map<String, Double> historique = monPortefeuille.getHistoriqueValeurs();
        if (historique.isEmpty()) return;

        long joursPositifs = historique.values().stream()
                .filter(v -> v > (monPortefeuille.calculerValeurTotale() - AnalyseurPerformance.calculerPlusValueTotale(monPortefeuille)))
                .count();

        double ratio = (double) joursPositifs / historique.size() * 100;
        System.out.println(String.format("Analyse : Portefeuille bénéficiaire à %.1f%% du temps.", ratio));
    }

    private void appliquerStyleProfit(double profit) {
        profitLabel.getStyleClass().removeAll("profit-positive", "profit-negative", "text-green", "text-red");
        profitPercentageLabel.getStyleClass().removeAll("profit-pill-positive", "profit-pill-negative");

        if (profit >= 0) {
            profitLabel.getStyleClass().add("text-green");
            profitPercentageLabel.getStyleClass().add("profit-pill-positive");
        } else {
            profitLabel.getStyleClass().add("text-red");
            profitPercentageLabel.getStyleClass().add("profit-pill-negative");
        }
    }

    private void initialiserVide() {
        totalValueLabel.setText("0.00 €");
        cashLabel.setText("0.00 €");
        profitLabel.setText("0.00 €");
        profitPercentageLabel.setText("0.00%");
        performanceChart.getData().clear();
        allocationChart.getData().clear();
    }
}