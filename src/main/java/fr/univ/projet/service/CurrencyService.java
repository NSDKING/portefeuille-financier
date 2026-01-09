package fr.univ.projet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyService {

    public static double convert(String from, String to, double amount) {
        try {
            String url = String.format("https://api.frankfurter.app/latest?from=%s&to=%s", from, to);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(response.body());

            double rate = data.get("rates").get(to).asDouble();
            double convertedAmount = amount * rate;

            System.out.printf("%.2f %s = %.2f %s (Taux: %.4f)%n", 
                              amount, from, convertedAmount, to, rate);
            
            return convertedAmount;
        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion : " + e.getMessage());
            return amount;  
        }
    }

    // Test rapide
    public static void main(String[] args) {
        convert("EUR", "USD", 10);
    }
}