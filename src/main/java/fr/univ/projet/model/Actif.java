
package fr.univ.projet.model;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class Actif {
    protected String nom;
    protected double prixActuel;
    protected String ticker;

    public Actif(String nom, double prixActuel, String ticker) {
        this.nom = nom;
        this.prixActuel = prixActuel;
        this.ticker = ticker;
    }

    public String getNom() {
        return nom;
    }

    public String getTicker() {
        return ticker;
    }

    public double getPrixActuel() {
        return prixActuel;
    }

    public void setPrixActuel(double prixActuel) {
        this.prixActuel = prixActuel;
    }

    public abstract double calculerValeurMonnaie(String monnaie, double quantite);
}