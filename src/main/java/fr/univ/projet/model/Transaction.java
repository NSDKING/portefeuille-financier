package fr.univ.projet.model;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private double quantite;
    private String date;
    private double frais;
    private double prixUnitaire;
    private List<Actif> actifs;

    public Transaction() {
        this.actifs = new ArrayList<>();
    }

    public Transaction(double prixUnitaire, double quantite, String date, double frais) {
        this.prixUnitaire = prixUnitaire;
        this.quantite = quantite;
        this.date = date;
        this.frais = frais;
        this.actifs = new ArrayList<>();
    }

    public double getPrixUnitaire() { return prixUnitaire; }
    public double getQuantite() { return quantite; }
    public double getFrais() { return frais; }
    public List<Actif> getActifs() { return actifs; }
    public String getDate() { return date; }

    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public void setQuantite(double quantite) { this.quantite = quantite; }
    public void setFrais(double frais) { this.frais = frais; }
    public void setActifs(List<Actif> actifs) { this.actifs = actifs; }
    public void setDate(String date) { this.date = date; }

    public void ajouterActif(Actif actif) {
        this.actifs.add(actif);
    }   
}