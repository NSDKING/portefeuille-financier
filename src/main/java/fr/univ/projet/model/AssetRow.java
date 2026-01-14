package fr.univ.projet.model;

import javafx.beans.property.*;

public class AssetRow {
    private final StringProperty symbol;
    private final DoubleProperty quantity;
    private final DoubleProperty currentPrice;
    private final DoubleProperty totalValue;

    public AssetRow(String symbol, double quantity, double currentPrice) {
        this.symbol = new SimpleStringProperty(symbol);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.currentPrice = new SimpleDoubleProperty(currentPrice);
        this.totalValue = new SimpleDoubleProperty(quantity * currentPrice);
    }

    // Getters pour JavaFX TableView
    public StringProperty symbolProperty() { return symbol; }
    public DoubleProperty quantityProperty() { return quantity; }
    public DoubleProperty currentPriceProperty() { return currentPrice; }
    public DoubleProperty totalValueProperty() { return totalValue; }
}