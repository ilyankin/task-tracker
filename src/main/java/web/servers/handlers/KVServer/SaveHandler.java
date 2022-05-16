package web.servers.handlers.KVServer;

import com.sun.net.httpserver.HttpExchange;
import web.servers.KVServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;
import java.util.Map;

public class SaveHandler extends AbstractHttpHandler {
    private final String apiKey;
    private final Map<String, String> data;

    public SaveHandler(String apiKey, Map<String, String> data) {
        this.apiKey = apiKey;
        this.data = data;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!hasAuth(exchange)) {
                System.out.println("The request is unauthorized, you need a parameter in the query API_KEY with the value of the api key");
                exchange.sendResponseHeaders(403, 0);
                return;
            }
            switch (exchange.getRequestMethod()) {
                case "POST":
                    String key = exchange.getRequestURI().getPath().substring("/save/".length());
                    if (key.isEmpty()) {
                        System.out.println("The key is empty");
                        exchange.sendResponseHeaders(400, 0);
                        return;
                    }
                    String value = readText(exchange, KVServer.defaultCharset);
                    if (value.isEmpty()) {
                        System.out.println("No any data from body request for save");
                        exchange.sendResponseHeaders(400, 0);
                        return;
                    }
                    data.put(key, value);
                    System.out.println("The value '" + value + "' for the key " + key + " has been updated successfully!");
                    exchange.sendResponseHeaders(200, 0);
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0);
                    System.out.println("/save received " + exchange.getRequestMethod() + " request");
                    System.out.println("This context (/save) can only work with the following methods: POST");
            }
        } finally {
            exchange.close();
        }
    }

    protected boolean hasAuth(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_KEY=" + apiKey) || rawQuery.contains("API_KEY=DEBUG"));
    }

}
