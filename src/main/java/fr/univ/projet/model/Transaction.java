package fr.univ.projet.model;

import java.util.List;

public class Transaction {
    private double quantite;
    private String date;
    private String frais;
    private double prixUnitaire;
    private List<Actif> actifs;


    public Transaction(double prixUnitaire, double quantite, String date, String frais) {
        this.prixUnitaire = prixUnitaire;
        this.quantite = quantite;
        this.date = date;
        this.frais = frais;
        this.actifs = new java.util.ArrayList<>();
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public double getQuantite() {
        return quantite;
    }

    public String getFrais() {
        return frais;
    }

    public List<Actif> getActifs() {
        return actifs;
    }

    public void ajouterActif(Actif actif) {
        this.actifs.add(actif);
    }   
 
    
    public String getDate() {
        return date;
    }

 
    
}
