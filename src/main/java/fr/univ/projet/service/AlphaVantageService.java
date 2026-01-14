import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class AlphaVantageService {

    private static final String API_KEY = "MSCHS5BZAXVU72QT"; 

    public static void getStockData(String symbol) {
        // Construction de l'URL
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" 
                     + symbol + "&apikey=" + API_KEY;

        // Création du client HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Construction de la requête
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            // Envoi de la requête et récupération de la réponse en String
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Affichage du JSON brut
            System.out.println("Code Statut : " + response.statusCode());
            System.out.println("Données : " + response.body());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        getStockData("IBM");
    }
}