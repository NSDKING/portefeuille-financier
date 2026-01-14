package fr.univ.projet.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Portefeuille implements Cloneable {
    private String nom;
    private String description;
    private String monnaieReference;
    private List<Transaction> transactions;
    private double cash;  
    private List<Event> events;  

    public Portefeuille() {
        this.transactions = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public Portefeuille(String nom, String description, String monnaieReference) {
        this(); 
        this.nom = nom;
        this.description = description;
        this.monnaieReference = monnaieReference;
    }

    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setMonnaieReference(String monnaieReference) { this.monnaieReference = monnaieReference; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public void setEvents(List<Event> events) { this.events = events; }

    public String getNom() { return nom; }
    public String getMonnaieReference() { return monnaieReference; }
    public String getDescription() { return description; }
    public List<Transaction> getTransactions() { return transactions; }
    public List<Event> getEvents() { return events; }
    public double getCash() { return cash; }
    public void setCash(double cash) { this.cash = cash; }

    public void ajouterTransaction(Transaction transaction) { transactions.add(transaction); }
    public void ajouterEvent(Event event) { events.add(event); }
    public void addTransaction(Transaction tx) {
        if (this.transactions == null) this.transactions = new java.util.ArrayList<>();
        this.transactions.add(tx);
    }

    public double calculerValeurTotale() {
        double valeurActifs = transactions.stream()
                .mapToDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                .sum();
        return valeurActifs + cash;
    }

    public Map<String, Double> getHistoriqueValeurs() {
        Map<String, Double> historique = new TreeMap<>();
        double cumul = cash;
        for (Transaction t : transactions) {
            cumul += (t.getQuantite() * t.getPrixUnitaire());
            historique.put(t.getDate(), cumul);
        }
        return historique;
    }

    @Override
    public Portefeuille clone() {
        try {
            Portefeuille clone = (Portefeuille) super.clone();
            clone.transactions = new ArrayList<>(this.transactions);
            clone.events = new ArrayList<>(this.events);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}