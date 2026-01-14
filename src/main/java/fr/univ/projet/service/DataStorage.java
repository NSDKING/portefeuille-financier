package fr.univ.projet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.univ.projet.model.Portefeuille;
import fr.univ.projet.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataStorage {
    
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) 
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Vérifie si le fichier chiffré de l'utilisateur existe déjà.
     */
    public static boolean userExists(String username) {
        return Files.exists(Paths.get(username + ".enc"));
    }

    /**
     * INSCRIPTION : Sauvegarde l'objet User complet (avec sa liste de portefeuilles)
     */
    public static void saveNewUser(String username, String password, User utilisateur) throws Exception {
        String jsonString = mapper.writeValueAsString(utilisateur);
        String encryptedData = SecurityService.encrypt(jsonString, password);
        Files.writeString(Paths.get(username + ".enc"), encryptedData);
    }

    /**
     * CONNEXION : Charge et déchiffre l'objet User.
     * C'est cette méthode que ton LoginController doit appeler.
     */
    public static User loadUserSecurely(String username, String password) throws Exception {
        String filename = username + ".enc";
        
        if (!Files.exists(Paths.get(filename))) {
            throw new IOException("Utilisateur introuvable.");
        }

        // 1. Lire le contenu chiffré
        String encryptedContent = Files.readString(Paths.get(filename));

        // 2. Déchiffrer avec le mot de passe
        String decryptedJson = SecurityService.decrypt(encryptedContent, password);

        // 3. Transformer le JSON en objet User
        return mapper.readValue(decryptedJson, User.class);
    }

    // --- Méthode de secours pour sauvegarder un User (utile après une transaction) ---
    public static void saveUser(User user, String password) throws Exception {
        saveNewUser(user.getUsername(), password, user);
    }
}