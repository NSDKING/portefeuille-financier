package fr.univ.projet.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.univ.projet.model.Crypto;
import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.Transaction;

public class AnalyseurPerformance {

    /**
     * Calcule la plus-value totale du portefeuille
     */
    public static double calculerPlusValueTotale(Portefeuille p) {
        double valeurActuelle = p.calculerValeurTotale();

        double coutTotal = p.getTransactions().stream()
                .mapToDouble(t -> t.getQuantite() * t.getPrixUnitaire())
                .sum();

        return valeurActuelle - coutTotal;
    }

    /**
     * Calcule la répartition du portefeuille (Crypto / Action)
     */
    public static Map<String, Double> calculerRepartition(Portefeuille p) {
        return p.getTransactions().stream()
                .collect(Collectors.groupingBy(
                        t -> t.getActifs().get(0) instanceof Crypto ? "Crypto" : "Action",
                        Collectors.summingDouble(
                                t -> t.getQuantite() * t.getActifs().get(0).getPrixActuel()
                        )
                ));
    }

    /**
     * Calcule la valeur du portefeuille à une date donnée
     * Date de transaction stockée sous forme String (yyyy-MM-dd)
     */
    public static double calculerValeurADate(Portefeuille p, LocalDate dateCible) {
        double total = 0.0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Transaction> transactionsAvantDate = p.getTransactions().stream()
                .filter(t -> {
                    LocalDate dateTransaction = LocalDate.parse(t.getDate(), formatter);
                    return !dateTransaction.isAfter(dateCible);
                })
                .collect(Collectors.toList());

        // Logique simplifiée (prix historique non implémenté)
        for (Transaction t : transactionsAvantDate) {
            total += t.getQuantite() * t.getPrixUnitaire();
        }

        return total;
    }
}
