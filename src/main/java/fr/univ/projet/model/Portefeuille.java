package fr.univ.projet.model;

import java.util.ArrayList;
import java.util.List;

public class Portefeuille implements Cloneable {
    private String nom;
    private String description;
    private String monnaieReference;
    private List<Transaction> transactions;
    private List<Event> events; // Pour les futurs événements liés au portefeuille

    public Portefeuille(String nom, String description, String monnaieReference) {
        this.nom = nom;
        this.description = description;
        this.monnaieReference = monnaieReference;
        this.transactions = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public String getNom() {
        return nom;
    }

    public String getMonnaieReference() {
        return monnaieReference;
    }
    
    public String getDescription() {
        return description;
    }

    public void ajouterTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void ajouterEvent(Event event) {
        events.add(event);
    }

    public List<Event> getEvents() {
        return events;
    }

    public double calculerValeurTotale() {
        // Logique : Somme des (quantité * prix actuel de l'actif)
        return transactions.stream()
                .mapToDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                .sum();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public Portefeuille clone() {
        try {
            Portefeuille clone = (Portefeuille) super.clone();
            clone.transactions = new ArrayList<>(this.transactions); // Copie superficielle des transactions
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    
}
