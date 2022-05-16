package web.servers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import exceptions.ManagerIllegalMethodRequestException;
import exceptions.ManagerTaskNotFoundException;
import managers.FileBackedTaskManager;
import managers.TaskManager;
import web.servers.handlers.httpTaskServer.*;
import web.servers.typeAdapters.DurationAdapter;
import web.servers.typeAdapters.ExceptionAdapter;
import web.servers.typeAdapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;


public class HttpTaskServer {
    public static Charset defaultCharset = StandardCharsets.UTF_8;
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(ManagerIllegalMethodRequestException.class, new ExceptionAdapter())
            .registerTypeAdapter(ManagerTaskNotFoundException.class, new ExceptionAdapter())
            .create();
    private final int port;
    private HttpServer httpTaskServer;
    private final TaskManager manager;

    public HttpTaskServer(TaskManager manager, int port, String hostname) {
        this.port = port;
        this.manager = manager;
        try {
            httpTaskServer = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        } catch (IOException e) {
            System.out.println("Oops, something went wrong when creating a HttpTaskServer on the port: " + port
                    + "and with hostname: " + hostname);
            e.printStackTrace();
        }
        loadContexts();
    }

    public HttpTaskServer(TaskManager manager, int port) {
        this(manager, port, "localhost");
    }

    public void start() {
        System.out.println("The HttpTaskServer is running on the port " + port);
        httpTaskServer.start();
    }

    public void stop(int delay) {
        httpTaskServer.stop(delay);
        System.out.println("The HttpTaskServer's been stopped on the port " + port);
    }

    public static void setDefaultCharset(Charset charset) {
        defaultCharset = charset;
    }

    private void loadContexts() {
        httpTaskServer.createContext("/tasks/task/", new TasksHandler(manager));
        httpTaskServer.createContext("/tasks/task", new TaskHandler(manager));
        httpTaskServer.createContext("/tasks/epic/", new EpicsHandler(manager));
        httpTaskServer.createContext("/tasks/epic", new EpicHandler(manager));
        httpTaskServer.createContext("/tasks/stories/epic", new StoriesHandler(manager));
        httpTaskServer.createContext("/tasks/story", new StoryHandler(manager));
        httpTaskServer.createContext("/tasks/history", new HistoryHandler((FileBackedTaskManager) manager));
        httpTaskServer.createContext("/tasks/", new PrioritizedTasksHandler((FileBackedTaskManager) manager));
    }

}
