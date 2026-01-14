package fr.univ.projet;

import java.util.List;

import fr.univ.projet.model.Actif;

public class AnalyzerPerformance {

    // ===================== ACTIF =====================

    // Gain ou perte d'un actif
    public static double gain(Actif actif) {
        return actif.getPrixActuel() - actif.getPrixInitiale();
    }

    // Rendement en pourcentage d'un actif
    public static double rendement(Actif actif) {
        return (gain(actif) / actif.getPrixInitiale()) * 100;
    }

    // ===================== PORTEFEUILLE =====================

    // Gain total du portefeuille
    public static double gainTotal(List<Actif> portefeuille) {
        double total = 0;
        for (Actif a : portefeuille) {
            total += gain(a);
        }
        return total;
    }

    // Valeur initiale totale
    public static double valeurInitialeTotale(List<Actif> portefeuille) {
        double total = 0;
        for (Actif a : portefeuille) {
            total += a.getPrixInitiale();
        }
        return total;
    }

    // Valeur actuelle totale
    public static double valeurActuelleTotale(List<Actif> portefeuille) {
        double total = 0;
        for (Actif a : portefeuille) {
            total += a.getPrixActuel();
        }
        return total;
    }

    // Rendement global du portefeuille (%)
    public static double rendementGlobal(List<Actif> portefeuille) {
        double initial = valeurInitialeTotale(portefeuille);
        double actuel = valeurActuelleTotale(portefeuille);
        if (portefeuille.isEmpty()) {
            return 0;
}
    return ((actuel - initial) / initial) * 100;
    }

    // Rendement moyen des actifs
    public static double rendementMoyen(List<Actif> portefeuille) {
        double total = 0;
        for (Actif a : portefeuille) {
            total += rendement(a);
        }
        if (portefeuille.isEmpty()) {
            return 0;
}
        return total / portefeuille.size();
    }
}
