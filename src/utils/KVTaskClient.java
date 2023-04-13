package utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private String serverURL;
    private String token;
    HttpClient client = HttpClient.newHttpClient();

    public KVTaskClient(String serverURL) throws IOException, InterruptedException {
        this.serverURL = serverURL;

        HttpRequest getTokenRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(serverURL + "/register"))
                .header("Content-type", "application/json")
                .build();

        HttpResponse<String> tokenResponse = client.send(
                getTokenRequest,
                HttpResponse.BodyHandlers.ofString());

        this.token = tokenResponse.body();
    }

    public boolean put(String key, String value) throws IOException, InterruptedException {
        boolean successSaving = false; // for defining if saving was successful
        URI saveUrl = URI.create(serverURL + "/save/" + key + "?API_TOKEN=" + token);
        HttpRequest saveRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(value))
                .uri(saveUrl)
                .header("Content-type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(
                    saveRequest,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println(this.getClass().getSimpleName() +  ": Save unsuccessful. Response code:  "
                        + response.statusCode());
                successSaving = false;
            }
            successSaving = true;
        } catch (IOException | InterruptedException e){
            System.out.println(this.getClass().getSimpleName() +  ": Save data fail");
            successSaving = false;
        }
        return successSaving;
    }

    public String load(String key)  {
        URI loadURL = URI.create(serverURL + "/load/" + key + "?API_TOKEN=" + token);
        HttpRequest loadRequest = HttpRequest.newBuilder()
                .GET()
                .uri(loadURL)
                .header("Content-type", "application/json")
                .build();
        try{
            HttpResponse<String> response = client.send(
                    loadRequest,
                    HttpResponse.BodyHandlers.ofString());
            if (response.body() == null){
                System.out.println(this.getClass().getSimpleName() +  ": Data loading failed");
                return null;
            }
            System.out.println(response.body());
            return response.body();
        } catch (IOException | InterruptedException e){
            System.out.println(this.getClass().getSimpleName() +  ": Data loading failed");
            return null;
        }
    }
}
