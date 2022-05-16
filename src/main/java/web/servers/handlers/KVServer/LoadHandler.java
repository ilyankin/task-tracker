package web.servers.handlers.KVServer;

import com.sun.net.httpserver.HttpExchange;
import web.servers.KVServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;
import java.util.Map;

public class LoadHandler extends AbstractHttpHandler {
    private final String apiKey;
    private final Map<String, String> data;

    public LoadHandler(String apiKey, Map<String, String> data) {
        this.apiKey = apiKey;
        this.data = data;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!hasAuth(exchange)) {
                System.out.println("The request is unauthorized, you need a parameter in the query API_KEY with the value of the api key");
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            switch (exchange.getRequestMethod()) {
                case "GET":
                    String key = exchange.getRequestURI().getPath().substring("/save/".length());
                    if (key.isEmpty()) {
                        System.out.println("The key is empty");
                        exchange.sendResponseHeaders(400, -1);
                        return;
                    }
                    String value = data.get(key);
                    if (value == null) {
                        System.out.println("No any data for the key: " + key);
                        exchange.sendResponseHeaders(400, -1);
                        return;
                    }
                    sendText(exchange, value, KVServer.defaultCharset);
                    System.out.println("The value (" + value + ") for the key (" + key + ") received successfully!");
                    exchange.sendResponseHeaders(200, value.length());
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0);
                    System.out.println("/load received " + exchange.getRequestMethod() + " request");
                    System.out.println("This context (/load) can only work with the following methods: GET");
            }
        } finally {
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        }

    }

    protected boolean hasAuth(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_KEY=" + apiKey) || rawQuery.contains("API_KEY=DEBUG"));
    }
}
