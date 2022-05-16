package web.servers;

import com.google.gson.Gson;
import managers.HTTPTaskManager;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import models.repositories.tasks.AbstractTasksRepository;
import models.repositories.tasks.CombinedTasksRepository;
import models.repositories.tasks.EpicsRepository;
import models.repositories.tasks.TasksRepository;
import models.tasks.AbstractTask;
import models.tasks.Epic;
import models.tasks.Story;
import models.tasks.Task;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import utils.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KVServerTest {
    private static final String URL = "http://localhost:8078";
    private static KVServer KVServer;
    private static HttpTaskServer httpTaskServer;
    private static HttpClient httpClient;

    private static TaskManager taskManager;
    private static Gson gson;


    @BeforeAll
    static void setUp() {
        KVServer = new KVServer(8078, "localhost", true);
        KVServer.start();
        httpClient = HttpClient.newHttpClient();
    }

    @BeforeEach
    void refreshData() {
        AbstractTasksRepository.TASK_COUNTER.reset();
        taskManager = Managers.getDefault();
        httpTaskServer = new HttpTaskServer(taskManager, 8080);
        httpTaskServer.start();
        gson = new Gson();
    }

    @AfterEach
    void stopHttpTaskServer() {
        httpTaskServer.stop(0);
    }

    @AfterAll
    static void tearDown() {
        KVServer.stop(0);
    }


    private static final int OK = 200;

    @ParameterizedTest
    @ValueSource(strings = {"storage", "tasks", "epics", "stories", "history"})
    void shouldBeReturnEmptyValueByGetRequestWithKey(String keys) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/load/" + keys + "?API_KEY=DEBUG"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("[]", response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeReturnStorageByGetRequest() throws IOException, InterruptedException {
        taskManager.addTask(Task.createTask("Task"));
        taskManager.addEpic(Epic.createEpic("Epic"));
        taskManager.addStory(Story.createStory("Story", 2));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/load/storage?API_KEY=DEBUG"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        TasksRepository tasksRepository = ((InMemoryTaskManager) taskManager).getTasksRepository();
        EpicsRepository epicsRepository = ((InMemoryTaskManager) taskManager).getEpicsRepository();
        assertEquals(gson.toJson(CombinedTasksRepository.getInstance(tasksRepository, epicsRepository).getAbstractTasks()),
                response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeReturnTasksByGetRequest() throws IOException, InterruptedException {
        taskManager.addTask(Task.createTask("Task"));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/load/tasks?API_KEY=DEBUG"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(taskManager.findAllTasks()), response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeReturnEpicsByGetRequest() throws IOException, InterruptedException {
        taskManager.addEpic(Epic.createEpic("Epic"));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/load/epics?API_KEY=DEBUG"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(taskManager.findAllEpics()), response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeReturnStoriesByGetRequest() throws IOException, InterruptedException {
        taskManager.addEpic(Epic.createEpic("Epic"));
        taskManager.addStory(Story.createStory("Story", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/load/stories?API_KEY=DEBUG"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(((HTTPTaskManager) taskManager).findAllStories()), response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeReturnHistoryByGetRequest() throws IOException, InterruptedException {
        taskManager.addTask(Task.createTask("Task"));
        taskManager.addEpic(Epic.createEpic("Epic"));
        taskManager.addStory(Story.createStory("Story", 2));
        taskManager.findTask(1);
        taskManager.findStory(3);
        taskManager.findEpic(2);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/load/history?API_KEY=DEBUG"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(((HTTPTaskManager) taskManager).getHistoryManager().getHistory()
                .stream()
                .map(AbstractTask::getId)
                .collect(Collectors.toList())), response.body());
        assertEquals(OK, response.statusCode());
    }

}
