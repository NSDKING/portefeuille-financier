package fr.univ.projet.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Action")
public class Action extends Actif {
    private String entreprise;
    private String bourse; 
    private String deviseOrigine;  


    public Action(String ticker, String nom, double prixActuel, String deviseOrigine, String entreprise, String bourse, double prixInitiale) {
        super(nom, prixActuel, ticker, prixInitiale); 
        this.deviseOrigine = deviseOrigine;
        this.entreprise = entreprise;
        this.bourse = bourse;
    }

    // Getters
    public String getEntreprise() { return entreprise; }
    public String getBourse() { return bourse; }
    public String getDeviseOrigine() { return deviseOrigine; }

    public double getValeurActuelle() {
         return this.prixActuel;
    }

    @Override
    public double calculerValeurMonnaie(String monnaieRefPortefeuille, double quantite) {
         double valeurNative = this.prixActuel * quantite;

         if (this.deviseOrigine.equalsIgnoreCase(monnaieRefPortefeuille)) {
            return valeurNative;
        }

        // 3. Sinon, on applique le taux de change (David fournira le service)
        // double taux = DavidService.getTaux(this.deviseOrigine, monnaieRefPortefeuille);
        // return valeurNative * taux;
        
        return valeurNative; // En attente de l'implémentation du service de David
    }
}