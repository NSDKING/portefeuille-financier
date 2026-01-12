package fr.univ.projet.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class SecurityService {

    private static final String ALGORITHM = "AES";

    /**
     * Chiffre le JSON avec le mot de passe utilisateur
     */
    public static String encrypt(String data, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encVal);
    }

    /**
     * Déchiffre le fichier avec le mot de passe utilisateur
     */
    public static String decrypt(String encryptedData, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue, StandardCharsets.UTF_8);
    }

    /**
     * Génère une clé AES de 128 bits à partir du mot de passe
     */
    private static SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        // AES-128 utilise les 16 premiers octets (128 bits) du hash SHA-256
        return new SecretKeySpec(key, 0, 16, ALGORITHM);
    }
}