package web.servers.handlers.httpTaskServer;

import com.sun.net.httpserver.HttpExchange;
import exceptions.ManagerIllegalMethodRequestException;
import exceptions.ManagerTaskNotFoundException;
import managers.TaskManager;
import utils.Web;
import web.servers.HttpTaskServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static web.servers.HttpTaskServer.defaultCharset;

public class StoriesHandler extends AbstractHttpHandler {
    private final int ID_QUERY_INDEX = 0;
    private final TaskManager manager;

    public StoriesHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var queryParameters = Web.getQueryParameters(exchange.getRequestURI());
            Map.Entry<String, String> id;
            switch (exchange.getRequestMethod()) {
                case "GET":
                    if (queryParameters.isEmpty()) return;
                    id = queryParameters.get(ID_QUERY_INDEX);
                    if (!"id".equals(id.getKey())) return;
                    String response = HttpTaskServer.GSON.toJson(manager.findAllStories(Long.parseLong(id.getValue())));
                    sendText(exchange, response, HttpTaskServer.defaultCharset);
                    System.out.println("The stories were received successfully");
                    break;
                case "DELETE":
                    if (queryParameters.isEmpty()) return;
                    id = queryParameters.get(ID_QUERY_INDEX);
                    if (!"id".equals(id.getKey())) return;
                    manager.deleteAllStories(manager.findEpic(Long.parseLong(id.getValue())));
                    sendNoContentResponseHeaders(exchange);
                    System.out.println("The stories were removed successfully");
                default:
                    throw new ManagerIllegalMethodRequestException(exchange.getHttpContext().getPath(),
                            exchange.getRequestMethod(), "GET", "DELETE");
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            sendBadRequestResponseHeaders(exchange);
        } catch (ManagerTaskNotFoundException e) {
            e.printStackTrace();
            sendNotFoundResponseHeaders(exchange);
        } catch (ManagerIllegalMethodRequestException e) {
            sendText(exchange, HttpTaskServer.GSON.toJson(e), 405, defaultCharset);
        }
        finally {
            sendInternalServerErrorRequestResponseHeaders(exchange);
            exchange.close();
        }
    }
}
