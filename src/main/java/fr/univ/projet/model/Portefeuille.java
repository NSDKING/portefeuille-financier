package fr.univ.projet.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Portefeuille implements Cloneable {
    private String nom;
    private String description;
    private String monnaieReference;
    private List<Transaction> transactions;
    private double cash;  
    private List<Event> events;
    private double investissementInitial = 0.0;  // <- Champ ajouté

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

    // --- Getters & Setters ---
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMonnaieReference() { return monnaieReference; }
    public void setMonnaieReference(String monnaieReference) { this.monnaieReference = monnaieReference; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = events; }

    public double getCash() { return cash; }
    public void setCash(double cash) { this.cash = cash; }

    public double getInvestissementInitial() { return investissementInitial; }
    public void setInvestissementInitial(double investissementInitial) { this.investissementInitial = investissementInitial; }

    // --- Méthodes ---
    public void addTransaction(Transaction t) { transactions.add(t); }
    public void ajouterEvent(Event e) { events.add(e); }


    public double calculerCashFlowCumule() {
        return transactions.stream()
                .mapToDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                .sum();
    }

    public Map<LocalDate, Double> getHistoriqueValeurs() {
        Map<LocalDate, Double> historique = new TreeMap<>();
        double cumul = 0;

        for (Transaction t : transactions) {
            try {
                LocalDate date = LocalDate.parse(t.getDate());
                cumul += t.getQuantite() * t.getPrixUnitaire();
                historique.put(date, cumul);
            } catch (DateTimeParseException e) {
                System.out.println("Transaction ignorée (date invalide) : " + t.getDate());
            }
        }
        return historique;
    }

    public double calculerValeurTotale() {
        return transactions.stream()
                .mapToDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                .sum() + cash;
    }

    public double getInvestissementInitialJusqua(LocalDate date) {
        return transactions.stream()
                .filter(t -> !LocalDate.parse(t.getDate()).isAfter(date))
                .mapToDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                .sum();
    }

    public double calculerValeurMarcheActuelle() {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getActifs().get(0),
                        Collectors.summingDouble(Transaction::getQuantite)
                ))
                .entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrixActuel() * e.getValue())
                .sum() + cash;
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
