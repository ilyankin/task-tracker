package web.servers.handlers.httpTaskServer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import exceptions.ManagerIllegalMethodRequestException;
import exceptions.ManagerTaskNotFoundException;
import managers.TaskManager;
import models.tasks.Epic;
import models.tasks.Task;
import utils.Web;
import web.servers.HttpTaskServer;
import web.servers.handlers.AbstractHttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static web.servers.HttpTaskServer.defaultCharset;

public class TaskHandler extends AbstractHttpHandler {
    private final int ID_QUERY_INDEX = 0;
    private final TaskManager manager;

    public TaskHandler(TaskManager manager) {
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
                    String response = HttpTaskServer.GSON.toJson(manager.findTask(Long.parseLong(id.getValue())));
                    sendText(exchange, response, HttpTaskServer.defaultCharset);
                    System.out.println("The task's been received successfully");
                    break;
                case "PUT":
                    if ((contentTypeValues != null) && (contentTypeValues.contains("application/json"))) {
                        Task task = HttpTaskServer.GSON.fromJson(readText(exchange, HttpTaskServer.defaultCharset), Task.class);
                        manager.updateTask(task.getId(), task);
                        sendNoContentResponseHeaders(exchange);
                        System.out.println("The task's been updated successfully");
                    }
                    break;
                case "POST":
                    if ((contentTypeValues != null) && (contentTypeValues.contains("application/json"))) {
                        Task task = HttpTaskServer.GSON.fromJson(readText(exchange, HttpTaskServer.defaultCharset), Task.class);
                        manager.addTask(task);
                        sendCreatedResponseHeaders(exchange);
                        System.out.println("The task's been created successfully");
                    }
                    break;
                case "DELETE":
                    if (queryParameters.isEmpty()) return;
                    id = queryParameters.get(ID_QUERY_INDEX);
                    if (!"id".equals(id.getKey())) return;
                    manager.deleteTask(Long.parseLong(id.getValue()));
                    sendNoContentResponseHeaders(exchange);
                    System.out.println("The task's been updated successfully");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sendInternalServerErrorRequestResponseHeaders(exchange);
            exchange.close();
        }
    }
}
