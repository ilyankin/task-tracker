package web.clients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private HttpRequest request;
    private final HttpClient client;
    private String serverKey;
    private final URI url;

    public KVTaskClient(URI url) {
        client = HttpClient.newHttpClient();
        this.url = url;
        register();
    }

    private void register() {
        URI path = URI.create(url + "register");
        request = HttpRequest.newBuilder()
                .GET()
                .uri(path)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            serverKey = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void put(String key, String json) {
        URI path = URI.create(url + String.format("save/%s?API_KEY=%s", key, serverKey));
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(path)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String load(String key) {
        URI path = URI.create(url + String.format("load/%s?API_KEY=%s", key, serverKey));
        request = HttpRequest.newBuilder()
                .GET()
                .uri(path)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        String response = "";
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public URI getUrl() {
        return url;
    }
}
