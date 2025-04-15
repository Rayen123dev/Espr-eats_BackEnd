package tn.esprit.projet_pi.Service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AiSearchService {

    private static final String API_URL = "https://api-inference.huggingface.co/models/transformersbook/t5-small-qa-sql";
    private static final String HF_TOKEN = "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";

    public String getSqlCondition(String query) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = "{\"inputs\": \"translate English to SQL: " + query + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", HF_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}