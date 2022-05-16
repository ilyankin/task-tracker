package web.servers.handlers.httpTaskServer;

import com.sun.net.httpserver.HttpExchange;
import exceptions.ManagerIllegalMethodRequestException;
import managers.TaskManager;
import web.servers.HttpTaskServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;

import static web.servers.HttpTaskServer.defaultCharset;

public class EpicsHandler extends AbstractHttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    String response = HttpTaskServer.GSON.toJson(manager.findAllEpics());
                    sendText(exchange, response, HttpTaskServer.defaultCharset);
                    System.out.println("The epics were received successfully");
                    break;
                case "DELETE":
                    manager.deleteAllEpics();
                    sendNoContentResponseHeaders(exchange);
                    ;
                    System.out.println("The epics were removed successfully");
                default:
                    throw new ManagerIllegalMethodRequestException(exchange.getHttpContext().getPath(),
                            exchange.getRequestMethod(), "GET", "DELETE");
            }
        } catch (ManagerIllegalMethodRequestException e) {
            sendText(exchange, HttpTaskServer.GSON.toJson(e), 405, defaultCharset);
        }
        finally {
            sendInternalServerErrorRequestResponseHeaders(exchange);
            exchange.close();
        }
    }
}
