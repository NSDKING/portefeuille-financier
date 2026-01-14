package fr.univ.projet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;

public class AlphaVantageService {

    private static final String API_KEY = "MSCHS5BZAXVU72QT";
    private static final ObjectMapper mapper = new ObjectMapper();

 
    public static double getLatestPrice(String symbol) {
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" 
                     + symbol + "&apikey=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            // 1. Gestion des erreurs (Clé invalide, limite d'appels, etc.)
            if (root.has("Note") || root.has("Error Message")) {
                System.err.println("Erreur API : " + (root.has("Note") ? "Limite atteinte" : "Symbole invalide"));
                return -1.0;
            }

            // 2. Accès à la série temporelle
            JsonNode timeSeries = root.get("Time Series (Daily)");
            if (timeSeries != null && timeSeries.fieldNames().hasNext()) {
                String latestDate = timeSeries.fieldNames().next();
                
                return timeSeries.get(latestDate).get("4. close").asDouble();
            }

        } catch (Exception e) {
            System.err.println("Échec de la récupération du prix pour " + symbol);
        }
        return 0.0;
    }

    public static void main(String[] args) {
        // Test rapide
        double price = getLatestPrice("IBM");
        System.out.println("Dernier prix IBM : " + price + " USD");
    }
}