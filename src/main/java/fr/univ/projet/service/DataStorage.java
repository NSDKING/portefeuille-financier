package fr.univ.projet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.univ.projet.model.Portefeuille;

import java.io.File;
import java.io.IOException;

public class DataStorage {
    private static final ObjectMapper mapper = new ObjectMapper()
                                                        .enable(SerializationFeature.INDENT_OUTPUT);

    //sauvegarder un portefeuille dans un fichier JSON
    public static void savePortefeuille(Portefeuille p, String filename) {
        try {
            File dossier = new File("src/main/resources/data");
            if (!dossier.exists()) dossier.mkdirs();

            mapper.writeValue(new File(dossier, filename+ ".json"), p);
            System.out.println("Portefeuille sauvegardé avec succès !");

        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du portefeuille : " + e.getMessage());
        }
    }

    //charger un portefeuille depuis un fichier JSON
    public static Portefeuille loadPortfolio(String filename) {
        try {
            File file = new File("src/main/resources/data/" + filename + ".json");
            if (!file.exists()) {
                System.out.println("Fichier non trouvé, création d'un nouveau portefeuille.");
                return null;
            }
            return mapper.readValue(file, Portefeuille.class);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du portefeuille : " + e.getMessage());
            return null;
        }
    }
}