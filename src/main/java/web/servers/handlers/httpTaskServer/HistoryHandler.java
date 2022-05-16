package web.servers.handlers.httpTaskServer;

import com.sun.net.httpserver.HttpExchange;
import exceptions.ManagerIllegalMethodRequestException;
import managers.InMemoryTaskManager;
import models.tasks.AbstractTask;
import web.servers.HttpTaskServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;
import java.util.stream.Collectors;

import static web.servers.HttpTaskServer.defaultCharset;

public class HistoryHandler extends AbstractHttpHandler {
    private final InMemoryTaskManager manager;

    public HistoryHandler(InMemoryTaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    String response = HttpTaskServer.GSON.toJson(manager.getHistoryManager().getHistory()
                            .stream()
                            .map(AbstractTask::getId)
                            .collect(Collectors.toList()));
                    sendText(exchange, response, HttpTaskServer.defaultCharset);
                    System.out.println("The history was received successfully");
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
