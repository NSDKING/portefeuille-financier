package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import model.Actif;

public class Dashboard {

    private BorderPane root = new BorderPane();

    // Données
    private ObservableList<Actif> portefeuille = FXCollections.observableArrayList();

    // Table
    private TableView<Actif> table = new TableView<>();

    // Graphique
    private NumberAxis xAxis = new NumberAxis();
    private NumberAxis yAxis = new NumberAxis();
    private LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    private XYChart.Series<Number, Number> series = new XYChart.Series<>();

    public Dashboard() {
        creerTable();
        creerGraphique();
        creerBoutons();
        ajouterZoom();
    }

    // ================= TABLE =================

    private void creerTable() {

        TableColumn<Actif, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getNom())
        );

        TableColumn<Actif, Number> colInitiale = new TableColumn<>("Initiale");
        colInitiale.setCellValueFactory(data ->
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getValeurInitiale())
        );

        TableColumn<Actif, Number> colActuelle = new TableColumn<>("Actuelle");
        colActuelle.setCellValueFactory(data ->
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getValeurActuelle())
        );

        table.getColumns().addAll(colNom, colInitiale, colActuelle);
        table.setItems(portefeuille);

        root.setLeft(table);
    }

    // ================= GRAPHIQUE =================

    private void creerGraphique() {
        lineChart.setTitle("Évolution du portefeuille");
        xAxis.setLabel("Temps");
        yAxis.setLabel("Valeur");

        series.setName("Performance");
        lineChart.getData().add(series);

        root.setCenter(lineChart);
    }

    // ================= BOUTONS =================

    private void creerBoutons() {

        Button btnAjouter = new Button("Ajouter un actif");
        btnAjouter.setOnAction(e -> ajouterActif());

        HBox box = new HBox(10, btnAjouter);
        root.setBottom(box);
    }

    // ================= AJOUT ACTIF =================

    private void ajouterActif() {

        Dialog<Actif> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un actif");

        ButtonType valider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(valider, ButtonType.CANCEL);

        TextField nom = new TextField();
        TextField valeur = new TextField();

        dialog.getDialogPane().setContent(
            new VBox(10,
                new Label("Nom :"), nom,
                new Label("Valeur actuelle :"), valeur
            )
        );

        dialog.setResultConverter(b -> {
            if (b == valider) {
                return new Actif(nom.getText(), 0, Double.parseDouble(valeur.getText()));
            }
            return null;
        });

        dialog.showAndWait().ifPresent(actif -> {
            portefeuille.add(actif);
            ajouterPointGraphique(actif.getValeurActuelle());
        });
    }

    // ================= POINT + TOOLTIP =================

    private void ajouterPointGraphique(double valeur) {
        int x = series.getData().size() + 1;

        XYChart.Data<Number, Number> point =
                new XYChart.Data<>(x, valeur);

        series.getData().add(point);

        point.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip.install(newNode,
                        new Tooltip("Valeur : " + valeur));
            }
        });
    }

    // ================= ZOOM =================

    private void ajouterZoom() {
        lineChart.addEventFilter(ScrollEvent.SCROLL, e -> {
            double zoomFactor = (e.getDeltaY() > 0) ? 0.9 : 1.1;

            yAxis.setLowerBound(yAxis.getLowerBound() * zoomFactor);
            yAxis.setUpperBound(yAxis.getUpperBound() * zoomFactor);
        });
    }

    // ================= VIEW =================

    public BorderPane getView() {
        return root;
    }
}
