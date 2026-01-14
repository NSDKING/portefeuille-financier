package fr.univ.projet.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private List<Portefeuille> portefeuilles;

    public User() {
        this.portefeuilles = new ArrayList<>();
    }

    public User(String username) {
        this();
        this.username = username;
    }

    // Getters / Setters pour Jackson
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<Portefeuille> getPortefeuilles() { return portefeuilles; }
    public void setPortefeuilles(List<Portefeuille> portefeuilles) { this.portefeuilles = portefeuilles; }
}