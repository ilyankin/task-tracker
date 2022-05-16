package web.servers.handlers.httpTaskServer;

import com.sun.net.httpserver.HttpExchange;
import exceptions.ManagerIllegalMethodRequestException;
import exceptions.ManagerTaskNotFoundException;
import managers.TaskManager;
import models.tasks.Story;
import utils.Web;
import web.servers.HttpTaskServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static web.servers.HttpTaskServer.defaultCharset;

public class StoryHandler extends AbstractHttpHandler {
    private final int ID_QUERY_INDEX = 0;
    private final TaskManager manager;

    public StoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var queryParameters = Web.getQueryParameters(exchange.getRequestURI());
            Map.Entry<String, String> id;
            List<String> contentTypeValues = exchange.getRequestHeaders().get("content-type");
            switch (exchange.getRequestMethod()) {
                case "GET":
                    if (queryParameters.isEmpty()) return;
                    id = queryParameters.get(ID_QUERY_INDEX);
                    if (!"id".equals(id.getKey())) return;
                    String response = HttpTaskServer.GSON.toJson(manager.findStory(Long.parseLong(id.getValue())));
                    sendText(exchange, response, HttpTaskServer.defaultCharset);
                    System.out.println("The story's been received successfully");
                    break;
                case "PUT":
                    if ((contentTypeValues != null) && (contentTypeValues.contains("application/json"))) {
                        Story story = HttpTaskServer.GSON.fromJson(readText(exchange, defaultCharset), Story.class);
                        manager.updateStory(story.getId(), story);
                        sendNoContentResponseHeaders(exchange);
                        System.out.println("The story's been updated successfully");
                    }
                    break;
                case "POST":
                    if ((contentTypeValues != null) && (contentTypeValues.contains("application/json"))) {
                        Story story = HttpTaskServer.GSON.fromJson(readText(exchange, defaultCharset), Story.class);
                        manager.addStory(story);
                        sendCreatedResponseHeaders(exchange);
                        System.out.println("The story's been created successfully");
                    }
                    break;
                case "DELETE":
                    if (queryParameters.isEmpty()) return;
                    id = queryParameters.get(ID_QUERY_INDEX);
                    if (!"id".equals(id.getKey())) return;
                    manager.deleteStory(Long.parseLong(id.getValue()));
                    sendNoContentResponseHeaders(exchange);
                    System.out.println("The story's been updated successfully");
                    break;
                default:
                    throw new ManagerIllegalMethodRequestException(exchange.getHttpContext().getPath(),
                            exchange.getRequestMethod(), "GET", "POST", "PUT", "DELETE");
            }
        } catch (NumberFormatException e) {
            sendBadRequestResponseHeaders(exchange);
        } catch (ManagerTaskNotFoundException e) {
            sendText(exchange, HttpTaskServer.GSON.toJson(e), 404, defaultCharset);
        } catch (ManagerIllegalMethodRequestException e) {
            sendText(exchange, HttpTaskServer.GSON.toJson(e), 405, defaultCharset);
        } finally {
            sendInternalServerErrorRequestResponseHeaders(exchange);
            exchange.close();
        }
    }
}
