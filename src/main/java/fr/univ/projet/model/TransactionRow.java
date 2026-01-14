package fr.univ.projet.model;

import javafx.beans.property.*;

public class TransactionRow {
    private final StringProperty type;
    private final StringProperty date;
    private final StringProperty symbol;
    private final DoubleProperty quantity;
    private final DoubleProperty price;
    private final DoubleProperty fees;
    private final DoubleProperty total;

    public TransactionRow(String type, String date, String symbol, double quantity, double price, double fees) {
        this.type = new SimpleStringProperty(type);
        this.date = new SimpleStringProperty(date);
        this.symbol = new SimpleStringProperty(symbol);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.fees = new SimpleDoubleProperty(fees);
        this.total = new SimpleDoubleProperty((quantity * price) + fees);
    }

    // Getters pour les propriétés (nécessaires pour le TableView)
    public StringProperty typeProperty() { return type; }
    public StringProperty dateProperty() { return date; }
    public StringProperty symbolProperty() { return symbol; }
    public DoubleProperty quantityProperty() { return quantity; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty feesProperty() { return fees; }
    public DoubleProperty totalProperty() { return total; }
}