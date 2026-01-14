package fr.univ.projet.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.univ.projet.service.CurrencyService;
@JsonTypeName("Crypto")  
public class Crypto extends Actif {
    private String addresseBlockchain;
    private String reseau;  
    private String deviseOrigine;  

    public Crypto(String ticker, String nom, double prixActuel, String deviseOrigine, String addresseBlockchain, String reseau, double prixInitiale) {
        super(nom, prixActuel, ticker, prixInitiale);
        this.deviseOrigine = deviseOrigine;
        this.addresseBlockchain = addresseBlockchain;
        this.reseau = reseau;
    }

    // Getters
    public String getAddresseBlockchain() { return addresseBlockchain; }
    public String getReseau() { return reseau; }
    public String getDeviseOrigine() { return deviseOrigine; }

    public double getValeurActuelle() {
         return this.prixActuel;
    }

 
    public double calculerValeurMonnaie(String monnaieRef, double quantite) {  
        double valeurEnDeviseOrigine = this.prixActuel * quantite;

        if (this.deviseOrigine.equalsIgnoreCase(monnaieRef)) {
            return valeurEnDeviseOrigine;
        }
        
        return CurrencyService.convert(this.deviseOrigine, monnaieRef, valeurEnDeviseOrigine);  
    }
}