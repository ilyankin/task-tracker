package web.servers;

import com.google.gson.Gson;
import managers.HTTPTaskManager;
import managers.TaskManager;
import models.repositories.tasks.AbstractTasksRepository;
import models.tasks.AbstractTask;
import models.tasks.Epic;
import models.tasks.Story;
import models.tasks.Task;
import org.junit.jupiter.api.*;
import utils.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerTest {
    private static final String URL = "http://localhost:8080";
    private static KVServer KVServer;
    private static HttpTaskServer httpTaskServer;
    private static HttpClient httpClient;
    private static TaskManager taskManager;
    private static Gson gson;

    private static Task task;
    private static Epic epic;
    private static Story story;

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
        task = taskManager.addTask(Task.createTask("Task"));
        epic = taskManager.addEpic(Epic.createEpic("Epic"));
        story = taskManager.addStory(Story.createStory("Story", epic.getId()));
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

    private static final int NOT_FOUND = 404;
    private static final int OK = 200;

    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;


    @Test
    void shouldBeReturnTaskByGetRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/task?id=1"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Task taskFromServer = gson.fromJson(response.body(), Task.class);
        assertEquals(task, taskFromServer);
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeReturnEpicByGetRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/epic?id=2"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Epic epicFromServer = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic, epicFromServer);
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeReturnStoryByGetRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/story?id=3"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Story storyFromServer = gson.fromJson(response.body(), Story.class);
        assertEquals(story, storyFromServer);
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeReturnNotFoundIfTaskNotFoundById() throws IOException, InterruptedException {
        HttpRequest taskRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/task?id=10"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/epic?id=10"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpRequest storyRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/story?id=10"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> taskResponse = httpClient.send(taskRequest, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> epicResponse = httpClient.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> storyResponse = httpClient.send(storyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_FOUND, taskResponse.statusCode());
        assertEquals(NOT_FOUND, epicResponse.statusCode());
        assertEquals(NOT_FOUND, storyResponse.statusCode());
    }

    @Test
    void shouldBeAddTaskByPostRequest() throws IOException, InterruptedException {
        Task newTask = Task.createTask("NewTask");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(newTask)))
                .uri(URI.create(URL + "/tasks/task"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Task task = taskManager.findTask(4);
        assertEquals(4, task.getId());
        assertEquals(newTask.getName(), task.getName());
        assertEquals(CREATED, response.statusCode());
    }

    @Test
    void shouldBeAddEpicByPostRequest() throws IOException, InterruptedException {
        Epic t = Epic.createEpic("NewEpic");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t)))
                .uri(URI.create(URL + "/tasks/epic"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Epic epic = taskManager.findEpic(4);
        assertEquals(4, epic.getId());
        assertEquals(t.getName(), epic.getName());
        assertEquals(CREATED, response.statusCode());
    }

    @Test
    void shouldBeAddStoryByPostRequest() throws IOException, InterruptedException {
        Story s = Story.createStory("NewStory", epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(s)))
                .uri(URI.create(URL + "/tasks/story"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Story story = taskManager.findStory(4);
        assertEquals(4, story.getId());
        assertEquals(s.getName(), story.getName());
        assertEquals(CREATED, response.statusCode());
    }


    @Test
    void shouldBeUpdateTaskByPutRequest() throws IOException, InterruptedException {
        Task t = Task.createTask(task.getId(), "UpdatedTask", "It's an updated task");
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(t)))
                .uri(URI.create(URL + "/tasks/task"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Task updatedTask = taskManager.findTask(1);
        assertEquals(t, updatedTask);
        assertEquals(t.getName(), updatedTask.getName());
        assertEquals(t.getDescription(), updatedTask.getDescription());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeUpdateEpicByPutRequest() throws IOException, InterruptedException {
        Epic e = Epic.createEpic(epic.getId(), "UpdatedEpic", "It's an updated epic");
        e.addStory(story);
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(e)))
                .uri(URI.create(URL + "/tasks/epic"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Epic updatedEpic = taskManager.findEpic(2);
        assertEquals(e, updatedEpic);
        assertEquals(e.getName(), updatedEpic.getName());
        assertEquals(e.getDescription(), updatedEpic.getDescription());
        assertEquals(e.getStories().size(), updatedEpic.getStories().size());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeUpdateStoryByPutRequest() throws IOException, InterruptedException {
        Story s = Story.createStory(story.getId(), "UpdatedStory", "It's an updated story", epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(s)))
                .uri(URI.create(URL + "/tasks/story"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Story updatedEpic = taskManager.findStory(3);
        assertEquals(s, updatedEpic);
        assertEquals(s.getName(), updatedEpic.getName());
        assertEquals(s.getDescription(), updatedEpic.getDescription());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeDeleteTaskByDeleteRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(URL + "/tasks/task?id=1"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(0, taskManager.findAllTasks().size());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeDeleteEpicByDeleteRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(URL + "/tasks/epic?id=2"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(0, taskManager.findAllEpics().size());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeDeleteStoryByDeleteRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(URL + "/tasks/story?id=3"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(0, taskManager.findAllStories(epic.getId()).size());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeGetAllTasksByGetRequest() throws IOException, InterruptedException {
        taskManager.addTask(Task.createTask("NewTask"));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(taskManager.findAllTasks()), response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeGetAllEpicsByGetRequest() throws IOException, InterruptedException {
        taskManager.addEpic(Epic.createEpic("NewEpic"));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(taskManager.findAllEpics()), response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeGetAllStoriesByGetRequest() throws IOException, InterruptedException {
        taskManager.addStory(Story.createStory("NewStory", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/stories/epic?id=2"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(taskManager.findAllStories(epic.getId())), response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeDeleteAllTasksByDeleteRequest() throws IOException, InterruptedException {
        taskManager.addTask(Task.createTask("NewTask"));
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(URL + "/tasks/task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(taskManager.findAllTasks().isEmpty());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeDeleteAllEpicsByDeleteRequest() throws IOException, InterruptedException {
        taskManager.addEpic(Epic.createEpic("NewEpic"));
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(URL + "/tasks/epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(taskManager.findAllEpics().isEmpty());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeDeleteAllStoriesByDeleteRequest() throws IOException, InterruptedException {
        taskManager.addStory(Story.createStory("NewStory", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(URL + "/tasks/stories/epic?id=2"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(taskManager.findEpic(2).getStories().isEmpty());
        assertEquals(NO_CONTENT, response.statusCode());
    }

    @Test
    void shouldBeGetEmptyHistoryByGetRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/history"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("[]", response.body());
        assertEquals(OK, response.statusCode());
    }

    @Test
    void shouldBeGetHistoryByGetRequest() throws IOException, InterruptedException {
        taskManager.findEpic(2);
        taskManager.findTask(1);
        taskManager.findStory(3);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/history"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(((HTTPTaskManager) taskManager).getHistoryManager().getHistory()
                .stream()
                .map(AbstractTask::getId)
                .collect(Collectors.toList())), response.body());
        assertEquals(OK, response.statusCode());
    }


    @Test
    void shouldBeGetPrioritizedTasksByGetRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(URL + "/tasks/"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(gson.toJson(((HTTPTaskManager) taskManager).getPrioritizedTasks()), response.body());
        assertEquals(OK, response.statusCode());
    }
}
