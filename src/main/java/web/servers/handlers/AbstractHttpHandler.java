package web.servers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.Charset;

public abstract class AbstractHttpHandler implements HttpHandler {

    protected void sendText(HttpExchange exchange, String text, Charset charset) throws IOException {
        sendText(exchange, text, 200, charset);
        byte[] resp = text.getBytes(charset);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
    }

    protected void sendText(HttpExchange exchange, String text, int rCode, Charset charset) throws IOException {
        byte[] resp = text.getBytes(charset);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(rCode, resp.length);
        exchange.getResponseBody().write(resp);
    }

    protected String readText(HttpExchange exchange, Charset charset) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), charset);
    }

    protected void sendCreatedResponseHeaders(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, -1);
    }

    protected void sendNoContentResponseHeaders(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }

    protected void sendBadRequestResponseHeaders(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(400, -1);
    }

    protected void sendNotFoundResponseHeaders(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
    }

    protected void sendProxyAuthenticationRequiredResponseHeaders(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, -1);
    }

    protected void sendInternalServerErrorRequestResponseHeaders(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(500, -1);
    }

}
