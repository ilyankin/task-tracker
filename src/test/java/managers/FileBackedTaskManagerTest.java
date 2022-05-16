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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    protected static final String FILE_NAME_FOR_GENERALIZED_TEST = "test1";

    private FileBackedTaskManagerTest() {
        super(Managers.getFileBacked(Path.of(FILE_NAME_FOR_GENERALIZED_TEST)));
    }

    @AfterEach
    void tearDown() throws IOException {
        final Path pathDataFile = Path.of(FILE_NAME_FOR_GENERALIZED_TEST);
        if (Files.exists(pathDataFile)) Files.delete(pathDataFile);
    }

    // All tests are below check the state of FileBackedAppManager loading data from the file
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class NestedBackedTaskManagerTest {
        private static final String FILE_NAME_FOR_FILE_BACKED_TEST = "test2";

        private FileBackedTaskManager fileBackedTaskManager;

        @BeforeEach
        void setUp() {
            AbstractTasksRepository.TASK_COUNTER.reset();
            fileBackedTaskManager = Managers.getFileBacked(Path.of(FILE_NAME_FOR_FILE_BACKED_TEST));
        }

        @AfterEach
        void tearDown() throws IOException {
            final Path pathDataFile = Path.of(FILE_NAME_FOR_FILE_BACKED_TEST);
            if (Files.exists(pathDataFile)) Files.delete(pathDataFile);
        }

        @Test
        void checkStateManagerAfterLoading() {
            add(TypeTask.TASK, "Task");
            add(TypeTask.EPIC, "Epic");
            Task task = fileBackedTaskManager.findTask(1);
            Epic epic = fileBackedTaskManager.findEpic(2);
            add(TypeTask.STORY, "Story", epic);
            Story story = fileBackedTaskManager.findStory(3);
            fileBackedTaskManager.load();
            // Task assertions
            taskMandatoryAssertions(task, 1, "Task", TypeTask.TASK, StateTask.NEW);
            assertTrue(task.getDescription().isEmpty());
            // Epic assertions
            taskMandatoryAssertions(epic, 2, "Epic", TypeTask.EPIC, StateTask.NEW);
            assertEquals(fileBackedTaskManager.findStory(3), epic.getStory(3)); // repeated search to change the state of historyManager
            assertEquals(1, epic.getStories().size());
            assertTrue(epic.getDescription().isEmpty());
            // Story assertions
            taskMandatoryAssertions(story, 3, "Story", TypeTask.STORY, StateTask.NEW);
            assertEquals(fileBackedTaskManager.findEpic(2).getId(), story.getEpicId()); // repeated search to change the state of historyManager
            assertTrue(epic.getDescription().isEmpty());

            HistoryManager hm = fileBackedTaskManager.getHistoryManager();
            assertArrayEquals(new long[]{1, 3, 2}, hm.getHistory().stream().mapToLong(AbstractTask::getId).toArray());

            assertEquals(1, fileBackedTaskManager.getTasksRepository().size());
            assertEquals(1, fileBackedTaskManager.getEpicsRepository().size());
        }

        private void add(TypeTask typeTask, String name, Epic epic) {
            switch (typeTask) {
                case TASK:
                    fileBackedTaskManager.addTask(Task.createTask(name));
                    break;
                case EPIC:
                    fileBackedTaskManager.addEpic(Epic.createEpic(name));
                    break;
                case STORY:
                    fileBackedTaskManager.addStory(Story.createStory(name, epic.getId()));
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