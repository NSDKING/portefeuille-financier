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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import fr.univ.projet.model.User;
import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.Transaction;
import fr.univ.projet.service.AnalyseurPerformance;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController {

    // Éléments de structure
    @FXML private BorderPane rootPane;
    
    // Éléments de navigation (Sidebar)
    @FXML private Button btnDashboard;
    @FXML private Button btnAssets;
    @FXML private Button btnHistory;
    @FXML private Button btnSettings;

    // Éléments du Dashboard (Top Bar)
    @FXML private Label totalValueLabel, profitLabel, profitPercentageLabel, cashLabel;
    @FXML private ComboBox<Portefeuille> portfolioSelector; 
    @FXML private Button btnRefresh;

    // Graphiques
    @FXML private LineChart<String, Number> performanceChart;
    @FXML private PieChart allocationChart;
    
    private User currentUser;
    private Portefeuille monPortefeuille;
    private Node dashboardView; // Stocke la vue centrale par défaut (graphiques)

    /**
     * Appelé automatiquement par JavaFX lors du chargement du FXML
     */
    @FXML
    public void initialize() {
        // 1. Sauvegarder la vue dashboard (tout ce qui est dans <center> au début)
        dashboardView = rootPane.getCenter();

        // 2. Configurer les actions des boutons de la Sidebar
        btnDashboard.setOnAction(e -> showDashboard());
        btnAssets.setOnAction(e -> showAssetsPage());
        
        if (btnRefresh != null) {
            btnRefresh.setOnAction(e -> rafraichirInterface());
        }
    }

    /**
     * Initialise la session après le login
     */
    public void setUserSession(User user) {
        this.currentUser = user;
        setupPortfolioSelector();
    }

    // --- NAVIGATION ---

    private void showDashboard() {
        // On remet la vue dashboard sauvegardée au centre
        rootPane.setCenter(dashboardView);
        updateActiveButton(btnDashboard);
        rafraichirInterface();
    }

    private void showAssetsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/univ/projet/view/mes-actifs.fxml"));
            VBox assetsView = loader.load();

            // Injection du portefeuille dans le contrôleur de la page Actifs
            AssetsController controller = loader.getController();
            controller.setPortfolio(monPortefeuille);

            // Remplacer le contenu central
            rootPane.setCenter(assetsView);
            updateActiveButton(btnAssets);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur fatale : Impossible de charger mes-actifs.fxml");
        }
    }

    private void updateActiveButton(Button activeBtn) {
        // Liste de tous les boutons pour réinitialiser le style
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnAssets.getStyleClass().remove("nav-button-active");
        
        // Appliquer le style au bouton cliqué
        activeBtn.getStyleClass().add("nav-button-active");
    }

    // --- LOGIQUE DE DONNÉES ---

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

        // Calculs via les services
        double totalValeur = monPortefeuille.calculerValeurTotale();
        double profitTotal = AnalyseurPerformance.calculerPlusValueTotale(monPortefeuille);
        double cash = monPortefeuille.getCash();
        double investissementInitial = totalValeur - profitTotal;
        double pourcentage = (investissementInitial > 0) ? (profitTotal / investissementInitial) * 100 : 0;

        // Mise à jour des Labels
        String devise = monPortefeuille.getMonnaieReference();
        totalValueLabel.setText(String.format("%.2f %s", totalValeur, devise));
        cashLabel.setText(String.format("%.2f %s", cash, devise));
        profitLabel.setText(String.format("%+.2f %s", profitTotal, devise));
        profitPercentageLabel.setText(String.format("%+.2f%%", pourcentage));

        appliquerStyleProfit(profitTotal);

        // Mise à jour des graphiques seulement si on est sur la vue Dashboard
        if (rootPane.getCenter() == dashboardView) {
            majLineChart();
            majPieChart();
        }
    }

    private void majLineChart() {
        performanceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valeur (" + monPortefeuille.getMonnaieReference() + ")");

        monPortefeuille.getHistoriqueValeurs().forEach((date, valeur) -> {
            series.getData().add(new XYChart.Data<>(date, valeur));
        });
        performanceChart.getData().add(series);
    }

    private void majPieChart() {
        allocationChart.getData().clear();
        // Groupement simplifié pour l'exemple
        Map<String, Double> repartition = monPortefeuille.getTransactions().stream()
                .collect(Collectors.groupingBy(
                    t -> "Actif", 
                    Collectors.summingDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                ));
        
        repartition.forEach((label, valeur) -> 
            allocationChart.getData().add(new PieChart.Data(label, valeur))
        );
    }

    private void appliquerStyleProfit(double profit) {
        profitLabel.getStyleClass().removeAll("profit-positive", "profit-negative");
        profitLabel.getStyleClass().add(profit >= 0 ? "profit-positive" : "profit-negative");
    }
}