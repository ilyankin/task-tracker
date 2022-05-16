package web.servers.handlers.KVServer;

import com.sun.net.httpserver.HttpExchange;
import web.servers.KVServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;

public class RegisterHandler extends AbstractHttpHandler {
    private final String apiKey;

    public RegisterHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    sendText(exchange, apiKey, KVServer.defaultCharset);
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0);
                    System.out.println("/register received " + exchange.getRequestMethod() + " request");
                    System.out.println("This context (/register) can only work with the following methods: GET");
            }
        } finally {
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        }
    }
}
