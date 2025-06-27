package dev.pakn.competitioncubes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.HashMap;

import org.json.JSONObject;

public class WebRequests {
    public static String sendPostRequest(String url, HashMap<String,String> params, String bearer) throws IOException, InterruptedException {
        JSONObject paramsJson = new JSONObject(params);
        
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpReq = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer "+bearer)
            .POST(BodyPublishers.ofString(paramsJson.toString()))
            .build();

        HttpResponse<String> response = client.send(httpReq,
            HttpResponse.BodyHandlers.ofString());
    
        return response.body();
    }
}
