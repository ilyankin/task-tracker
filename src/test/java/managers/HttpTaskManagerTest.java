package managers;

import managers.history.HistoryManager;
import models.enums.StateTask;
import models.enums.TypeTask;
import models.repositories.tasks.AbstractTasksRepository;
import models.tasks.AbstractTask;
import models.tasks.Epic;
import models.tasks.Story;
import models.tasks.Task;
import org.junit.jupiter.api.*;
import utils.Managers;
import web.servers.KVServer;
import web.servers.KVServerTest;

import static org.junit.jupiter.api.Assertions.*;


public class HttpTaskManagerTest extends TaskManagerTest<HTTPTaskManager> {
    private static final int PORT = 8078;
    private static final String HOSTNAME = "localhost";
    private static KVServer kvServer;

    @BeforeAll
    static void setUp() {
        kvServer = new KVServer(PORT, HOSTNAME, true);
        kvServer.start();
    }

    @AfterAll
    static void tearDown() {
        kvServer.stop(0);
    }

    protected HttpTaskManagerTest() {
        super(Managers.getDefault());
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class NestedHttpTaskManagerTest {
        private HTTPTaskManager httpTaskManager;

        @BeforeEach
        void setUp() {
            AbstractTasksRepository.TASK_COUNTER.reset();
            httpTaskManager = Managers.getDefault();
        }

        @Test
        void checkStateManagerAfterLoading() {
            add(TypeTask.TASK, "Task");
            add(TypeTask.EPIC, "Epic");
            Task task = httpTaskManager.findTask(1);
            Epic epic = httpTaskManager.findEpic(2);
            add(TypeTask.STORY, "Story", epic);
            Story story = httpTaskManager.findStory(3);
            httpTaskManager.load();
            // Task assertions
            taskMandatoryAssertions(task, 1, "Task", TypeTask.TASK, StateTask.NEW);
            assertTrue(task.getDescription().isEmpty());
            // Epic assertions
            taskMandatoryAssertions(epic, 2, "Epic", TypeTask.EPIC, StateTask.NEW);
            assertEquals(httpTaskManager.findStory(3), epic.getStory(3)); // repeated search to change the state of historyManager
            assertEquals(1, epic.getStories().size());
            assertTrue(epic.getDescription().isEmpty());
            // Story assertions
            taskMandatoryAssertions(story, 3, "Story", TypeTask.STORY, StateTask.NEW);
            assertEquals(httpTaskManager.findEpic(2).getId(), story.getEpicId()); // repeated search to change the state of historyManager
            assertTrue(epic.getDescription().isEmpty());

            HistoryManager hm = httpTaskManager.getHistoryManager();
            assertArrayEquals(new long[]{1, 3, 2}, hm.getHistory().stream().mapToLong(AbstractTask::getId).toArray());

            assertEquals(1, httpTaskManager.getTasksRepository().size());
            assertEquals(1, httpTaskManager.getEpicsRepository().size());
        }

        private void add(TypeTask typeTask, String name, Epic epic) {
            switch (typeTask) {
                case TASK:
                    httpTaskManager.addTask(Task.createTask(name));
                    break;
                case EPIC:
                    httpTaskManager.addEpic(Epic.createEpic(name));
                    break;
                case STORY:
                    httpTaskManager.addStory(Story.createStory(name, epic.getId()));
                    break;
            }
        }

        private void add(TypeTask typeTask, String name) {
            add(typeTask, name, null);
        }

        private void taskMandatoryAssertions(AbstractTask task, long id, String name, TypeTask tt, StateTask st) {
            assertEquals(id, task.getId());
            assertEquals(name, task.getName());
            assertEquals(tt, task.getTypeTask());
            assertEquals(st, task.getStateTask());
        }
    }
}
