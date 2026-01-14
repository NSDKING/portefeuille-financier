package fr.univ.projet.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import fr.univ.projet.model.User;
import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.Event;
import fr.univ.projet.service.AnalyseurPerformance;

import java.util.Map;
import java.util.stream.Collectors;

public class MainController {

    @FXML private Label totalValueLabel, profitLabel, profitPercentageLabel, cashLabel;
    @FXML private LineChart<String, Number> performanceChart;
    @FXML private PieChart allocationChart;
    @FXML private ComboBox<Portefeuille> portfolioSelector; 
    @FXML private Button btnRefresh;

    private User currentUser;
    private Portefeuille monPortefeuille;

    /**
     * SOLUTION : Cette méthode reçoit l'utilisateur après le Login.
     */
    public void setUserSession(User user) {
        this.currentUser = user;
        System.out.println("Session chargée pour : " + user.getUsername());
        
        // On initialise le sélecteur avec les portefeuilles créés à l'inscription
        setupPortfolioSelector();
    }

    private void setupPortfolioSelector() {
        portfolioSelector.getItems().setAll(currentUser.getPortefeuilles());

        // Pour afficher le nom du portefeuille dans la ComboBox
        portfolioSelector.setConverter(new StringConverter<Portefeuille>() {
            @Override
            public String toString(Portefeuille p) { return (p == null) ? "" : p.getNom(); }
            @Override
            public Portefeuille fromString(String s) { return null; }
        });

        portfolioSelector.setOnAction(e -> {
            this.monPortefeuille = portfolioSelector.getValue();
            rafraichirInterface();
        });

        // Sélection par défaut
        if (!currentUser.getPortefeuilles().isEmpty()) {
            portfolioSelector.setValue(currentUser.getPortefeuilles().get(0));
            this.monPortefeuille = currentUser.getPortefeuilles().get(0);
            rafraichirInterface();
        }
    }

    @FXML
    public void initialize() {
        // Ne plus appeler chargerDonnees() ici, car 'user' est injecté juste après initialize
        if (btnRefresh != null) {
            btnRefresh.setOnAction(e -> rafraichirInterface());
        }
    }

    private void rafraichirInterface() {
        if (monPortefeuille == null) return;

        double totalValeur = monPortefeuille.calculerValeurTotale();
        double profitTotal = AnalyseurPerformance.calculerPlusValueTotale(monPortefeuille);
        double cash = monPortefeuille.getCash();
        double investissementInitial = totalValeur - profitTotal;
        double pourcentage = (investissementInitial > 0) ? (profitTotal / investissementInitial) * 100 : 0;

        String devise = monPortefeuille.getMonnaieReference();
        totalValueLabel.setText(String.format("%.2f %s", totalValeur, devise));
        cashLabel.setText(String.format("%.2f %s", cash, devise));
        profitLabel.setText(String.format("%+.2f %s", profitTotal, devise));
        profitPercentageLabel.setText(String.format("%+.2f%%", pourcentage));

        appliquerStyleProfit(profitTotal);
        majLineChart();
        majPieChart();
    }

    // --- Les méthodes majLineChart, majPieChart et appliquerStyleProfit restent identiques ---
    private void majLineChart() {
        performanceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valeur (" + monPortefeuille.getMonnaieReference() + ")");

        monPortefeuille.getHistoriqueValeurs().forEach((date, valeur) -> {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(date, valeur);
            series.getData().add(dataPoint);
            // Tooltip logic...
        });
        performanceChart.getData().add(series);
    }

    private void majPieChart() {
        allocationChart.getData().clear();
        Map<String, Double> repartition = monPortefeuille.getTransactions().stream()
                .collect(Collectors.groupingBy(t -> "Actif", Collectors.summingDouble(t -> t.getQuantite() * t.getPrixUnitaire())));
        repartition.forEach((label, valeur) -> allocationChart.getData().add(new PieChart.Data(label, valeur)));
    }

    private void appliquerStyleProfit(double profit) {
        profitLabel.getStyleClass().removeAll("text-green", "text-red");
        profitLabel.getStyleClass().add(profit >= 0 ? "text-green" : "text-red");
    }
}