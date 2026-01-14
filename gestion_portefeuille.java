ArrayList<Actif>

package model;

public class Actif {

    private String nom;
    private double valeurInitiale;
    private double valeurActuelle;

    public Actif(String nom, double valeurInitiale, double valeurActuelle) {
        this.nom = nom;
        this.valeurInitiale = valeurInitiale;
        this.valeurActuelle = valeurActuelle;
    }

    public String getNom() {
        return nom;
    }

    public double getValeurInitiale() {
        return valeurInitiale;
    }

    public double getValeurActuelle() {
        return valeurActuelle;
    }
}


package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import model.Actif;

public class Dashboard {

    private BorderPane root = new BorderPane();

    // Liste observable = mise à jour automatique du tableau
    private ObservableList<Actif> portefeuille = FXCollections.observableArrayList();

    private TableView<Actif> table = new TableView<>();

    public Dashboard() {
        creerTable();
        creerBoutons();
    }

    private void creerTable() {

        TableColumn<Actif, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getNom())
        );

        TableColumn<Actif, Number> colInitiale = new TableColumn<>("Valeur initiale");
        colInitiale.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getValeurInitiale())
        );

        TableColumn<Actif, Number> colActuelle = new TableColumn<>("Valeur actuelle");
        colActuelle.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getValeurActuelle())
        );

        table.getColumns().addAll(colNom, colInitiale, colActuelle);
        table.setItems(portefeuille);

        root.setCenter(table);
    }

    private void creerBoutons() {

        Button btnAjouter = new Button("Ajouter un actif");
        Button btnSupprimer = new Button("Supprimer l'actif");

        btnAjouter.setOnAction(e -> ajouterActif());
        btnSupprimer.setOnAction(e -> supprimerActif());

        HBox box = new HBox(10, btnAjouter, btnSupprimer);
        root.setBottom(box);
    }

    // ===================== CRUD =====================

    private void ajouterActif() {

        Dialog<Actif> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un actif");

        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        TextField nomField = new TextField();
        TextField initialField = new TextField();
        TextField actuelField = new TextField();

        dialog.getDialogPane().setContent(
            new VBox(10,
                new Label("Nom :"), nomField,
                new Label("Valeur initiale :"), initialField,
                new Label("Valeur actuelle :"), actuelField
            )
        );

        dialog.setResultConverter(button -> {
            if (button == btnValider) {
                return new Actif(
                    nomField.getText(),
                    Double.parseDouble(initialField.getText()),
                    Double.parseDouble(actuelField.getText())
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(actif -> portefeuille.add(actif));
    }

    private void supprimerActif() {
        Actif selection = table.getS

