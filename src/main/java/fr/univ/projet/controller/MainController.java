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
import fr.univ.projet.service.AnalyzerPerformance; // Utilisation de votre classe AnalyzerPerformance
 
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController {

    @FXML private BorderPane rootPane;
    
    // Navigation
    @FXML private Button btnDashboard, btnAssets, btnHistory, btnSettings;

    // Indicateurs de performance
    @FXML private Label totalValueLabel, profitLabel, profitPercentageLabel, cashLabel;
    @FXML private Label analysisStatusLabel; // Label pour l'analyse créative (Bénéficiaire/Déficit)
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
            public String toString(Portefeuille p) { return (p == null) ? "" : p.getDescription(); }
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
        if (currentUser == null || currentUser.getPortefeuilles().isEmpty()) return;

        double totalCash = 0;
        double totalTitres = 0;
        double totalInvesti = 0;

        // 1️⃣ Calcul des totaux sur tous les portefeuilles
        for (Portefeuille p : currentUser.getPortefeuilles()) {
            totalCash += p.getCash();
            totalTitres += p.calculerValeurTotale() - p.getCash(); // juste la valeur des actifs
            totalInvesti += p.getInvestissementInitial();          // somme de l'investissement initial
        }

        // 2️⃣ Calcul du profit global et du pourcentage
        double profitTotal = totalTitres + totalCash - totalInvesti;
        double pourcentagePerf = (totalInvesti > 0) ? (profitTotal / totalInvesti) * 100 : 0;

        // 3️⃣ Mise à jour de l'UI
        String devise = currentUser.getPortefeuilles().get(0).getMonnaieReference(); // on suppose la même monnaie
        totalValueLabel.setText(String.format("%.2f %s", totalTitres + totalCash, devise));
        cashLabel.setText(String.format("%.2f %s", totalCash, devise));
        profitLabel.setText(String.format("%+.2f %s", profitTotal, devise));
        profitPercentageLabel.setText(String.format("%+.2f%%", pourcentagePerf));

        appliquerStyleProfit(profitTotal);

        // 4️⃣ Analyse créative sur chaque portefeuille
        if (analysisStatusLabel != null) {
            double ratioTempsVertTotal = 0;
            for (Portefeuille p : currentUser.getPortefeuilles()) {
                Map<String, Object> analyse = AnalyzerPerformance.analyserConstancePerformance(p);
                ratioTempsVertTotal += (double) analyse.get("pourcentageTempsVert");
            }
            double moyenneTempsVert = ratioTempsVertTotal / currentUser.getPortefeuilles().size();
            analysisStatusLabel.setText(String.format("Profil global : %.0f%% du temps bénéficiaire", moyenneTempsVert));
            analysisStatusLabel.setStyle("-fx-text-fill: " + (moyenneTempsVert >= 50 ? "#10b981" : "#ef4444"));
        }

        // 5️⃣ Mise à jour des graphiques si le dashboard est affiché
        if (rootPane.getCenter() == dashboardView) {
            majLineChart();
            majPieChart();
        }
    }

    private void majPieChart() {
        allocationChart.getData().clear();
        
        // Groupement par Ticker (Actif réel)
        Map<String, Double> repartition = monPortefeuille.getTransactions().stream()
                .filter(t -> t.getActifs() != null && !t.getActifs().isEmpty())
                .collect(Collectors.groupingBy(
                    t -> t.getActifs().get(0).getTicker(),
                    Collectors.summingDouble(t -> Math.abs(t.getQuantite()) * t.getActifs().get(0).getPrixActuel())
                ));
        
        repartition.forEach((ticker, valeur) -> {
            PieChart.Data data = new PieChart.Data(ticker, valeur);
            allocationChart.getData().add(data);
            
            Tooltip.install(data.getNode(), new Tooltip(String.format("%s: %.2f %s", 
                ticker, valeur, monPortefeuille.getMonnaieReference())));
        });
    }

    private void majLineChart() {
    performanceChart.getData().clear();

    // For each portefeuille, create a separate series
    for (Portefeuille p : currentUser.getPortefeuilles()) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(p.getDescription()); // The line will be named after the portfolio

        // For each date, calculate profit/delta for this portfolio
        p.getHistoriqueValeurs().forEach((date, valeurTotale) -> {
            double invested = p.getInvestissementInitialJusqua(date);
            double delta = valeurTotale - invested;
            series.getData().add(new XYChart.Data<>(date.toString(), delta));
        });

        performanceChart.getData().add(series);
    }
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