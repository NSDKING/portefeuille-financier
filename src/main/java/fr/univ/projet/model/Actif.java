
package fr.univ.projet.model;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class Actif {
    protected String nom;
    protected double prixActuel;
    protected String ticker;
    protected double prixInitiale;

    public Actif(String nom, double prixActuel, String ticker, double prixInitiale) {
        this.nom = nom;
        this.prixActuel = prixActuel;
        this.ticker = ticker;
        this.prixInitiale = prixInitiale;
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

    public double getPrixInitiale() {
        return prixInitiale;
    }
    
    public void setPrixInitiale(double prixInitiale) {
        this.prixInitiale = prixInitiale;
    }
    public abstract double calculerValeurMonnaie(String monnaie, double quantite);
}