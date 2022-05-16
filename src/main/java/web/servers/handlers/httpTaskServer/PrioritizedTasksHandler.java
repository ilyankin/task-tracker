package web.servers.handlers.httpTaskServer;

import com.sun.net.httpserver.HttpExchange;
import exceptions.ManagerIllegalMethodRequestException;
import managers.InMemoryTaskManager;
import web.servers.HttpTaskServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;

import static web.servers.HttpTaskServer.defaultCharset;

public class PrioritizedTasksHandler extends AbstractHttpHandler {
    private final InMemoryTaskManager manager;

    public PrioritizedTasksHandler(InMemoryTaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    String response = HttpTaskServer.GSON.toJson(manager.getPrioritizedTasks());
                    sendText(exchange, response, HttpTaskServer.defaultCharset);
                    System.out.println("The prioritized tasks were received successfully");
                    break;
                default:
                    throw new ManagerIllegalMethodRequestException(exchange.getHttpContext().getPath(),
                            exchange.getRequestMethod(), "GET");
            }
        } catch (ManagerIllegalMethodRequestException e) {
            sendText(exchange, HttpTaskServer.GSON.toJson(e), 405, defaultCharset);
        } finally {
            sendInternalServerErrorRequestResponseHeaders(exchange);
            exchange.close();
        }
    }
}
