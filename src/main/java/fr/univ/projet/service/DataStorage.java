package fr.univ.projet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Important pour les dates
import fr.univ.projet.model.Portefeuille;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataStorage {
    
    // Configuration du Mapper avec support des dates Java 8+
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) 
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * INSCRIPTION : Crée le fichier initial chiffré pour un nouvel utilisateur.
     */
    public static void saveNewUser(String username, String password, Portefeuille portefeuille) throws Exception {
        // 1. Convertir l'objet Portefeuille en JSON String
        String jsonString = mapper.writeValueAsString(portefeuille);

        // 2. Chiffrer cette String avec le mot de passe via SecurityService
        String encryptedData = SecurityService.encrypt(jsonString, password);

        // 3. Écrire le résultat dans un fichier .enc à la racine
        Files.writeString(Paths.get(username + ".enc"), encryptedData);
        System.out.println("Compte sécurisé créé pour : " + username);
    }

    /**
     * CONNEXION : Charge et déchiffre le portefeuille de l'utilisateur.
     */
    public static Portefeuille loadUserSecurely(String username, String password) throws Exception {
        String filename = username + ".enc";
        
        if (!Files.exists(Paths.get(filename))) {
            throw new IOException("Utilisateur introuvable.");
        }

        // 1. Lire le contenu chiffré
        String encryptedContent = Files.readString(Paths.get(filename));

        // 2. Déchiffrer avec le mot de passe
        String decryptedJson = SecurityService.decrypt(encryptedContent, password);

        // 3. Transformer le JSON en objet Portefeuille
        return mapper.readValue(decryptedJson, Portefeuille.class);
    }

    // --- Garde tes anciennes méthodes uniquement pour le debug ou l'export ---
    public static void savePortefeuille(Portefeuille p, String filename) {
        try {
            File dossier = new File("src/main/resources/data");
            if (!dossier.exists()) dossier.mkdirs();
            mapper.writeValue(new File(dossier, filename + ".json"), p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}