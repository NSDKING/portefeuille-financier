package fr.univ.projet.model;

import java.time.LocalDate;

public class Event {
    private LocalDate date;
    private String label;
    private String impact;
    private double impactEstime; // ex: -0.15 pour une baisse de 15%
    private String type;         // "CRIPTO", "BOURSE", ou "GLOBAL"

    public Event(LocalDate date, String label, String impact, double impactEstime, String type) {
        this.date = date;
        this.label = label;
        this.impact = impact;
        this.impactEstime = impactEstime;
        this.type = type;
    }

    // Getters indispensables pour David (JSON) et Salom (UI)
    public LocalDate getDate() { return date; }
    public String getLabel() { return label; }
    public double getImpactEstime() { return impactEstime; }
    public String getType() { return type; }
    public String getImpact() { return impact; }
}