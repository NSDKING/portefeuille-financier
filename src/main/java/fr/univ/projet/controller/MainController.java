package fr.univ.projet.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import fr.univ.projet.model.User;
import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.Transaction;
import fr.univ.projet.service.AnalyseurPerformance;
import fr.univ.projet.service.SessionManager;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController {

    @FXML private BorderPane rootPane;
    
    // Navigation
    @FXML private Button btnDashboard, btnAssets, btnHistory, btnSettings;

    // Indicateurs de performance
    @FXML private Label totalValueLabel, profitLabel, profitPercentageLabel, cashLabel;
    @FXML private ComboBox<Portefeuille> portfolioSelector; 
    @FXML private Button btnRefresh;

    // Graphiques
    @FXML private LineChart<String, Number> performanceChart;
    @FXML private PieChart allocationChart;
    
    private User currentUser;
    private Portefeuille monPortefeuille;
    private Node dashboardView; 

    @FXML
    public void initialize() {
        dashboardView = rootPane.getCenter();

        // Configuration des boutons de navigation
        btnDashboard.setOnAction(e -> showDashboard());
        btnAssets.setOnAction(e -> showAssetsPage());
        btnHistory.setOnAction(e -> showHistoryPage());
        
        if (btnRefresh != null) {
            btnRefresh.setOnAction(e -> rafraichirInterface());
        }
    }

    public void setUserSession(User user) {
        this.currentUser = user;
        setupPortfolioSelector();
    }

    // --- NAVIGATION ---

    private void showDashboard() {
        rootPane.setCenter(dashboardView);
        updateActiveButton(btnDashboard);
        rafraichirInterface();
    }

    private void showAssetsPage() {
        loadPage("/fr/univ/projet/view/mes-actifs.fxml", btnAssets, true);
    }

    private void showHistoryPage() {
        loadPage("/fr/univ/projet/view/history.fxml", btnHistory, false);
    }

    private void loadPage(String fxmlPath, Button btn, boolean injectPortfolio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            VBox view = loader.load();
            
            if (injectPortfolio) {
                AssetsController controller = loader.getController();
                controller.setPortfolio(monPortefeuille);
            }

            rootPane.setCenter(view);
            updateActiveButton(btn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateActiveButton(Button activeBtn) {
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnAssets.getStyleClass().remove("nav-button-active");
        btnHistory.getStyleClass().remove("nav-button-active");
        activeBtn.getStyleClass().add("nav-button-active");
    }

    // --- LOGIQUE DE DONNÉES & GRAPHIQUES ---

    private void setupPortfolioSelector() {
        if (currentUser == null) return;
        portfolioSelector.getItems().setAll(currentUser.getPortefeuilles());
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

        if (!currentUser.getPortefeuilles().isEmpty()) {
            portfolioSelector.setValue(currentUser.getPortefeuilles().get(0));
            this.monPortefeuille = currentUser.getPortefeuilles().get(0);
            rafraichirInterface();
        }
    }

    private void rafraichirInterface() {
        if (monPortefeuille == null) return;

        // 1. Calculs des indicateurs
        double totalTitres = monPortefeuille.calculerValeurTotale(); // Valeur actuelle au marché
        double cash = monPortefeuille.getCash();
        double valeurTotaleAffiche = totalTitres + cash; // Patrimoine total

        double profitTotal = AnalyseurPerformance.calculerPlusValueTotale(monPortefeuille);
        double coutInvesti = totalTitres - profitTotal;
        double pourcentagePerf = (coutInvesti > 0) ? (profitTotal / coutInvesti) * 100 : 0;

        // 2. Mise à jour de l'affichage
        String devise = monPortefeuille.getMonnaieReference();
        totalValueLabel.setText(String.format("%.2f %s", valeurTotaleAffiche, devise));
        cashLabel.setText(String.format("%.2f %s", cash, devise));
        profitLabel.setText(String.format("%+.2f %s", profitTotal, devise));
        profitPercentageLabel.setText(String.format("%+.2f%%", pourcentagePerf));

        appliquerStyleProfit(profitTotal);

        if (rootPane.getCenter() == dashboardView) {
            majLineChart();
            majPieChart();
        }
    }

    private void majPieChart() {
        allocationChart.getData().clear();
        
        // Groupement par Ticker des actifs réellement possédés
        Map<String, Double> repartition = monPortefeuille.getTransactions().stream()
                .filter(t -> t.getActifs() != null && !t.getActifs().isEmpty())
                .collect(Collectors.groupingBy(
                    t -> t.getActifs().get(0).getTicker(),
                    Collectors.summingDouble(t -> Math.abs(t.getQuantite()) * t.getActifs().get(0).getPrixActuel())
                ));
        
        repartition.forEach((ticker, valeur) -> {
            PieChart.Data data = new PieChart.Data(ticker, valeur);
            allocationChart.getData().add(data);
            
            // Ajout d'un tooltip pour voir la valeur exacte au survol
            Tooltip.install(data.getNode(), new Tooltip(String.format("%s: %.2f %s", 
                ticker, valeur, monPortefeuille.getMonnaieReference())));
        });
    }

    private void majLineChart() {
        performanceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valeur historique");

        monPortefeuille.getHistoriqueValeurs().forEach((date, valeur) -> {
            series.getData().add(new XYChart.Data<>(date, valeur));
        });
        performanceChart.getData().add(series);
    }

    private void appliquerStyleProfit(double profit) {
        profitLabel.getStyleClass().removeAll("profit-positive", "profit-negative");
        profitPercentageLabel.getStyleClass().removeAll("profit-pill-positive", "profit-pill-negative");
        
        if (profit >= 0) {
            profitLabel.getStyleClass().add("profit-positive");
            profitPercentageLabel.getStyleClass().add("profit-pill-positive");
        } else {
            profitLabel.getStyleClass().add("profit-negative");
            profitPercentageLabel.getStyleClass().add("profit-pill-negative");
        }
    }
}