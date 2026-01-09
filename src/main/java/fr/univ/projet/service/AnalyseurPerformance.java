package fr.univ.projet.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.sql.Date;
import fr.univ.projet.model.Crypto;
import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.Transaction;

public class AnalyseurPerformance {

    public static double calculerPlusValueTotale(Portefeuille p) {
        double valeurActuelle = p.calculerValeurTotale();
        double coutTotal = p.getTransactions().stream()
                            .mapToDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                            .sum();
        return valeurActuelle - coutTotal;
    }

    public static Map<String, Double> calculerRepartition(Portefeuille p) {
        return p.getTransactions().stream()
            .collect(Collectors.groupingBy(
                t -> t.getActifs().get(0) instanceof Crypto ? "Crypto" : "Action",
                Collectors.summingDouble(t -> t.getQuantite() * t.getActifs().get(0).getPrixActuel())
            ));
    }

    public static double calculerValeurADate(Portefeuille p, LocalDate dateCible) {
        double total = 0;
        // On filtre les transactions pour ne garder que celles passées avant la date
        List<Transaction> histo = p.getTransactions().stream()
                                    .filter(t -> Date.valueOf(t.getDate()).toLocalDate().isBefore(dateCible) || Date.valueOf(t.getDate()).toLocalDate().isEqual(dateCible))
                                    .toList();
        
        // Logique simplifiée : Somme des quantités à cette date * prix à cette date
        // (David devra fournir le prix historique via une nouvelle méthode)
        return total; 
    }
}