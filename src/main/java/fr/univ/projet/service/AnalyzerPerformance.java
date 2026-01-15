package fr.univ.projet.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import fr.univ.projet.model.Actif;
import fr.univ.projet.model.Portefeuille;

public class AnalyzerPerformance {

    /**
     * ANALYSE CRÉATIVE :
     * Le portefeuille a-t-il été plus souvent bénéficiaire ou déficitaire ?
     */
    public static Map<String, Object> analyserConstancePerformance(Portefeuille p) {

        if (p == null || p.getHistoriqueValeurs().isEmpty()) {
            return Map.of(
                "statut", "Inconnu",
                "pourcentageTempsVert", 0.0
            );
        }

        Map<LocalDate, Double> historique = p.getHistoriqueValeurs();

        long nbJoursBeneficiaire = historique.entrySet().stream()
            .filter(entry -> {
                LocalDate date = entry.getKey();
                double valeur = entry.getValue();

                double investiJusquaDate = p.getInvestissementInitialJusqua(date);
                return valeur > investiJusquaDate;
            })
            .count();

        double ratio = (double) nbJoursBeneficiaire / historique.size();

        return Map.of(
            "statut", ratio >= 0.5
                ? "Majoritairement Bénéficiaire"
                : "Majoritairement en Déficit",
            "pourcentageTempsVert", ratio * 100
        );
    }

    // ===================== ANALYSE ACTIFS =====================

    public static double gain(Actif actif) {
        return actif.getPrixActuel() - actif.getPrixInitiale();
    }

    public static double rendement(Actif actif) {
        if (actif.getPrixInitiale() == 0) return 0;
        return (gain(actif) / actif.getPrixInitiale()) * 100;
    }

    public static double valeurActuelleTotale(List<Actif> actifs) {
        return actifs.stream()
            .mapToDouble(Actif::getPrixActuel)
            .sum();
    }

    public static double calculerPlusValueTotale(Portefeuille p) {
        return p.calculerValeurMarcheActuelle()
             - p.getInvestissementInitial();
    }
}
