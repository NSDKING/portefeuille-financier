package fr.univ.projet.service;

import fr.univ.projet.model.User;

public class SessionManager {
    private static User currentUser;
    private static String currentPassword;

    public static void connect(User user, String password) {
        currentUser = user;
        currentPassword = password;
    }

    public static User getCurrentUser() { return currentUser; }
    public static String getCurrentPassword() { return currentPassword; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public static void setCurrentPassword(String password) { currentPassword = password; }
}