package service;

import model.Actif;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportCSV {

    public static void exporterPortefeuille(List<Actif> portefeuille, String nomFichier) {

        try (FileWriter writer = new FileWriter(nomFichier)) {

            // En-tête
            writer.write("Nom,Valeur initiale,Valeur actuelle,Gain,Rendement (%)\n");

            // Données
            for (Actif a : portefeuille) {
                double gain = AnalyzerPerformance.gain(a);
                double rendement = AnalyzerPerformance.rendement(a);

                writer.write(
                        a.getNom() + "," +
                        a.getValeurInitiale() + "," +
                        a.getValeurActuelle() + "," +
                        gain + "," +
                        rendement + "\n"
                );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
