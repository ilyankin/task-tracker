package managers;

import exceptions.ManagerIntersectionTaskIntervalsException;
import exceptions.ManagerTaskNotFoundException;
import managers.history.HistoryManager;
import models.enums.StateTask;
import models.enums.TypeTask;
import models.repositories.tasks.AbstractTasksRepository;
import models.repositories.tasks.EpicsRepository;
import models.repositories.tasks.TasksRepository;
import models.tasks.AbstractTask;
import models.tasks.Epic;
import models.tasks.Story;
import models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    private final T appManager;

    protected TaskManagerTest(T appManager) {
        this.appManager = appManager;
    }

    private final static List<Task> tasks = List.of(
            Task.createTask("Task1"),
            Task.createTask("Task2")
    );

    private final static List<Epic> epics = List.of(
            Epic.createEpic("Epic1"),
            Epic.createEpic("Epic2")
    );

    @BeforeEach
    void initRepositories() {
        AbstractTasksRepository.TASK_COUNTER.reset();
        appManager.createRepository(tasks, TasksRepository.class);
        appManager.createRepository(epics, EpicsRepository.class);
    }

    @Test
    void findTask() {
        final Task task = appManager.findTask(1);
        assertEquals(1, task.getId());
        assertEquals("Task1", task.getName());
        assertEquals(TypeTask.TASK, task.getTypeTask());
        assertEquals(StateTask.NEW, task.getStateTask());
        assertTrue(task.getDescription().isEmpty());
    }

    @Test
    void findTaskByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class, () -> appManager.findTask(3));
    }

    @Test
    void addTask() {
        final Task task3 = appManager.addTask(Task.createTask("Task3"));
        assertEquals(task3, appManager.findTask(5));
        assertEquals(3, appManager.findAllTasks().size());
    }

    @Test
    void updateTask() {
        final Task task = appManager.updateTask(2,
                Task.createTask(3, "UpdatedTaskName", "Updated task!", StateTask.IN_PROGRESS));
        final Task updatedTask = appManager.findTask(2);
        assertEquals(task, updatedTask);
        assertEquals("UpdatedTaskName", updatedTask.getName());
        assertEquals(StateTask.IN_PROGRESS, updatedTask.getStateTask());
        assertEquals("Updated task!", updatedTask.getDescription());
    }

    @Test
    void updateTaskByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class, () -> appManager.updateTask(3, Task.createTask("NewTask")));
    }

    @Test
    void deleteTask() {
        final Task task2 = appManager.deleteTask(2);
        assertEquals(2, task2.getId());
        assertEquals(1, appManager.findAllTasks().size());
    }

    @Test
    void deleteTaskByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class, () -> appManager.deleteTask(3));
    }

    @Test
    void deleteAllTasks() {
        appManager.deleteAllTasks();
        assertEquals(0, appManager.findAllTasks().size());
    }

    @Test
    void findEpic() {
        final Epic epic1 = appManager.findEpic(3);
        assertEquals(3, epic1.getId());
        assertEquals("Epic1", epic1.getName());
        assertEquals(TypeTask.EPIC, epic1.getTypeTask());
        assertEquals(StateTask.NEW, epic1.getStateTask());
        assertEquals(0, epic1.getStories().size());
        assertTrue(epic1.getDescription().isEmpty());
    }

    @Test
    void findEpicByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class, () -> appManager.findEpic(5));
    }

    @Test
    void addEpic() {
        final Epic epic3 = appManager.addEpic(Epic.createEpic("Epic3"));
        assertEquals(epic3, appManager.findEpic(5));
        assertEquals(3, appManager.findAllEpics().size());
    }

    @Test
    void updateEpic() {
        final Epic epic = appManager.updateEpic(3,
                Epic.createEpic(5, "UpdatedEpicName", StateTask.IN_PROGRESS, "Updated epic!"));
        final Epic updatedEpic = appManager.findEpic(3);
        assertEquals(epic, updatedEpic);
        assertEquals("UpdatedEpicName", updatedEpic.getName());
        assertEquals(StateTask.NEW, updatedEpic.getStateTask()); // Because the stateTask for epics is calculated automatically
        assertEquals("Updated epic!", updatedEpic.getDescription());
    }

    @Test
    void updateEpicByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class, () -> appManager.updateEpic(5, Epic.createEpic("NewEpic")));
    }

    @Test
    void deleteEpic() {
        final Epic epic2 = appManager.deleteEpic(4);
        assertEquals(4, epic2.getId());
        assertEquals(1, appManager.findAllEpics().size());
    }

    @Test
    void deleteEpicByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class, () -> appManager.deleteEpic(5));
    }

    @Test
    void deleteAllEpics() {
        appManager.deleteAllEpics();
        assertEquals(0, appManager.findAllEpics().size());
    }

    @Test
    void findStory() {
        final Epic epic1 = appManager.findEpic(3);
        final Story story3 = appManager.addStory(Story.createStory("Story1", "StoryOwnedEpic1", epic1.getId()));
        final Story story1 = appManager.findStory(5);
        assertEquals(story3, appManager.findStory(5));
        assertEquals("Story1", story1.getName());
        assertEquals("StoryOwnedEpic1", story1.getDescription());
        assertEquals(TypeTask.STORY, story1.getTypeTask());
        assertEquals(StateTask.NEW, story1.getStateTask());
    }

    @Test
    void addStory() {
        final Epic epic1 = appManager.findEpic(3);
        final Story story3 = appManager.addStory(Story.createStory("Story1", "StoryOwnedEpic1", epic1.getId()));
        assertEquals(story3, appManager.findStory(5));
        assertEquals(1, appManager.findAllStories(epic1.getId()).size());
    }

    @Test
    void findStoryByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class, () -> appManager.findEpic(5));
    }

    @Test
    void updateStory() {
        final Epic epic1 = appManager.findEpic(3);
        appManager.addStory(Story.createStory("Story1", "StoryOwnedEpic1", epic1.getId()));
        final Epic epic2 = appManager.findEpic(4);
        appManager.updateStory(5,
                Story.createStory(10, "UpdatedEpicName", "StoryOwnedEpic1", epic2.getId(), StateTask.DONE));
        final Story updatedStory = appManager.findStory(5);
        assertEquals(5, updatedStory.getId());
        assertEquals("UpdatedEpicName", updatedStory.getName());
        assertEquals("StoryOwnedEpic1", updatedStory.getDescription());
        assertEquals(epic2.getId(), updatedStory.getEpicId());
        assertEquals(StateTask.DONE, updatedStory.getStateTask());
        assertEquals(epic2.getId(), epic2.getStory(5).getEpicId());
        assertEquals(0, appManager.findEpic(3).getStories().size());
        assertEquals(1, appManager.findEpic(4).getStories().size());
    }

    @Test
    void updateStoryByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class,
                () -> appManager.updateStory(5, Story.createStory("NewStory", 3)));
    }

    @Test
    void deleteStory() {
        final Epic epic1 = appManager.findEpic(3);
        final Story story3 = appManager.addStory(Story.createStory("Story1", "StoryOwnedEpic1", epic1.getId()));
        final Story story1 = appManager.deleteStory(5);
        assertEquals(story3, story1);
        assertEquals(0, appManager.findAllStories(story1.getEpicId()).size());
    }

    @Test
    void deleteStoryByWrongId() {
        assertThrows(ManagerTaskNotFoundException.class, () -> appManager.deleteStory(5));
    }

    @Test
    void deleteAllStories() {
        final Epic epic1 = appManager.findEpic(3);
        appManager.deleteAllStories(appManager.findEpic(3));
        assertEquals(0, appManager.findAllStories(epic1.getId()).size());
    }

    private static Stream<Arguments> storyStatusProvider() {
        return Stream.of(
                // stateTasksStories, expectedStateTaskEpic
                Arguments.of(new StateTask[]{}, StateTask.NEW),
                Arguments.of(new StateTask[]{StateTask.NEW}, StateTask.NEW),
                Arguments.of(new StateTask[]{StateTask.IN_PROGRESS}, StateTask.IN_PROGRESS),
                Arguments.of(new StateTask[]{StateTask.DONE}, StateTask.DONE),
                Arguments.of(new StateTask[]{StateTask.NEW, StateTask.NEW}, StateTask.NEW),
                Arguments.of(new StateTask[]{StateTask.DONE, StateTask.DONE}, StateTask.DONE),
                Arguments.of(new StateTask[]{StateTask.IN_PROGRESS, StateTask.IN_PROGRESS}, StateTask.IN_PROGRESS),
                Arguments.of(new StateTask[]{StateTask.NEW, StateTask.IN_PROGRESS}, StateTask.IN_PROGRESS),
                Arguments.of(new StateTask[]{StateTask.NEW, StateTask.DONE}, StateTask.IN_PROGRESS),
                Arguments.of(new StateTask[]{StateTask.NEW, StateTask.IN_PROGRESS, StateTask.DONE}, StateTask.IN_PROGRESS)
        );
    }

    @ParameterizedTest
    @MethodSource("storyStatusProvider")
    void checkDependenciesOfEpicStatusOnStoriesStatus(StateTask[] stateTasksStories, StateTask expectedStateTaskEpic) {
        final Epic epic = appManager.findEpic(3);
        for (int i = 0; i < stateTasksStories.length; i++) {
            Story story = Story.createStory("StoryName" + (i + 1), epic.getId());
            story.setStateTask(stateTasksStories[i]);
            appManager.addStory(story);
        }
        assertEquals(expectedStateTaskEpic, epic.getStateTask());
    }

    private static Stream<Arguments> tasksFindSequenceDataProvider() {
        return Stream.of(
                // findTasks, idsTasksDeleteFromHistory, expectedHistoryManagerIds
                Arguments.of(new Object[][]{}, new long[]{}, new long[]{}),
                Arguments.of(new Object[][]{}, new long[]{1, 2}, new long[]{}),
                Arguments.of(new Object[][]{
                                {5L, TypeTask.STORY},
                                {4L, TypeTask.EPIC},
                                {3L, TypeTask.EPIC},
                                {2L, TypeTask.TASK},
                                {1L, TypeTask.TASK}},
                        new long[]{},
                        new long[]{5L, 4L, 3L, 2L, 1L}),
                Arguments.of(new Object[][]{
                                {1L, TypeTask.TASK},
                                {3L, TypeTask.EPIC},
                                {3L, TypeTask.EPIC},
                                {5L, TypeTask.STORY},
                                {5L, TypeTask.STORY},
                                {5L, TypeTask.STORY},
                                {2L, TypeTask.TASK},
                                {2L, TypeTask.TASK},
                                {1L, TypeTask.TASK},},
                        new long[]{},
                        new long[]{3L, 5L, 2L, 1L}),
                Arguments.of(new Object[][]{
                                {5L, TypeTask.STORY},
                                {4L, TypeTask.EPIC},
                                {3L, TypeTask.EPIC},
                                {2L, TypeTask.TASK},
                                {1L, TypeTask.TASK}},
                        new long[]{1L, 2L, 3L, 4L, 5L},
                        new long[]{}),
                Arguments.of(new Object[][]{
                                {5L, TypeTask.STORY},
                                {4L, TypeTask.EPIC},
                                {3L, TypeTask.EPIC},
                                {2L, TypeTask.TASK},
                                {1L, TypeTask.TASK}},
                        new long[]{5L, 1L},
                        new long[]{4L, 3L, 2}),
                Arguments.of(new Object[][]{
                                {5L, TypeTask.STORY},
                                {4L, TypeTask.EPIC},
                                {3L, TypeTask.EPIC},
                                {2L, TypeTask.TASK},
                                {1L, TypeTask.TASK}},
                        new long[]{3L},
                        new long[]{5L, 4L, 2L, 1L})
        );
    }

    @ParameterizedTest
    @MethodSource("tasksFindSequenceDataProvider")
    void checkHistoryManagerState(Object[][] findTasks, long[] idsTasksDeleteFromHistory,
                                  long[] expectedHistoryManagerIds) {
        if (findTasks.length != 0) {
            appManager.addStory(Story.createStory("Story1", "StoryOwnedEpic1", 3));
        }
        findTasks(appManager, findTasks);
        final InMemoryTaskManager inMemoryTaskManager = (InMemoryTaskManager) appManager;
        final List<AbstractTask> tasksFromHistory =
                deleteTasksFromHistory(inMemoryTaskManager.getHistoryManager(), idsTasksDeleteFromHistory).getHistory();
        final long[] historyManagerIds = new long[expectedHistoryManagerIds.length];
        for (int i = 0; i < historyManagerIds.length; i++) {
            historyManagerIds[i] = tasksFromHistory.get(i).getId();
        }
        assertArrayEquals(expectedHistoryManagerIds, historyManagerIds);
    }

    private void findTasks(T appManager, Object[][] findTasks) {
        for (Object[] findTask : findTasks) {
            final long id = (long) findTask[0];
            final TypeTask typeTask = (TypeTask) findTask[1];
            switch (typeTask) {
                case TASK:
                    appManager.findTask(id);
                    break;
                case EPIC:
                    appManager.findEpic(id);
                    break;
                case STORY:
                    appManager.findStory(id);
                    break;
            }
        }
    }

    private HistoryManager deleteTasksFromHistory(HistoryManager historyManager, long[] idsTasksDeleteFromHistory) {
        for (long id : idsTasksDeleteFromHistory) {
            historyManager.remove(id);
        }
        return historyManager;
    }


    private static Stream<Arguments> tasksStartTimeAndDurationDataProvider() {
        // LocalDateTime, Duration, TypeTask, expectedPrioritizedTasks
        return Stream.of(
                Arguments.of(
                        new Object[][]{
                                {null, null, TypeTask.TASK}, // id = 5
                                {null, null, TypeTask.STORY, 3L}, // id = 6
                        },
                        new long[]{1L, 2L, 5L, 6L}),
                Arguments.of(
                        new Object[][]{
                                {LocalDateTime.of(2020, Month.APRIL, 10, 12, 0, 0),
                                        Duration.ofDays(1).plusHours(1).plusMinutes(30).plusSeconds(20),
                                        TypeTask.TASK}, // id = 5
                                {LocalDateTime.of(2020, Month.APRIL, 11, 13, 30, 21),
                                        Duration.ofDays(0).plusHours(1).plusMinutes(30).plusSeconds(0),
                                        TypeTask.STORY,
                                        3L}, // id = 6
                                {LocalDateTime.of(2020, Month.MARCH, 25, 11, 10, 40),
                                        Duration.ofDays(1).plusHours(2).plusMinutes(30).plusSeconds(20),
                                        TypeTask.STORY,
                                        3L}, // id = 7
                                {LocalDateTime.of(2020, Month.MARCH, 28, 20, 10, 40),
                                        Duration.ofDays(5),
                                        TypeTask.STORY,
                                        4L}, // id = 8
                        },
                        new long[]{7L, 8L, 5L, 6L, 1L, 2L}),
                Arguments.of(new Object[][]{
                                {LocalDateTime.of(2020, Month.APRIL, 2, 12, 0, 0),
                                        Duration.ofDays(1),
                                        TypeTask.TASK}, // id = 5
                                {LocalDateTime.of(2020, Month.APRIL, 1, 12, 0, 0),
                                        Duration.ofDays(1),
                                        TypeTask.STORY,
                                        3L}, // id = 6
                                {LocalDateTime.of(2020, Month.APRIL, 3, 12, 0, 0),
                                        Duration.ofDays(1),
                                        TypeTask.STORY,
                                        4L}, // id = 7
                        },
                        new long[]{6L, 5L, 7L, 1L, 2L}),
                Arguments.of(new Object[][]{
                                {LocalDateTime.of(2020, Month.APRIL, 2, 12, 0, 0),
                                        Duration.ofDays(1),
                                        TypeTask.TASK}, // id = 5
                                {LocalDateTime.of(2020, Month.APRIL, 1, 12, 0, 0),
                                        Duration.ofDays(1).plusNanos(1),
                                        TypeTask.STORY,
                                        3L}, // id = 6
                                {LocalDateTime.of(2020, Month.APRIL, 2, 23, 59, 59),
                                        Duration.ofDays(1),
                                        TypeTask.STORY,
                                        4L}, // id = 7
                        },
                        new long[]{5L, 1L, 2L})
        );
    }

    @ParameterizedTest
    @MethodSource("tasksStartTimeAndDurationDataProvider")
    public void testPrioritizedTasks(Object[][] addTasks, long[] expectedPrioritizedTasksIds) {
        try {
            addTasks(appManager, addTasks);
        } catch (ManagerIntersectionTaskIntervalsException ignored) {
        }

        final InMemoryTaskManager inMemoryTaskManager = (InMemoryTaskManager) appManager;

        assertArrayEquals(expectedPrioritizedTasksIds, inMemoryTaskManager.getPrioritizedTasks().stream()
                .map(AbstractTask::getId)
                .mapToLong(l -> l)
                .toArray());
    }

    private void addTasks(T appManager, Object[][] addTasks) {
        for (Object[] addTask : addTasks) {
            final LocalDateTime startTime = (LocalDateTime) addTask[0];
            final Duration duration = (Duration) addTask[1];
            final TypeTask typeTask = (TypeTask) addTask[2];
            switch (typeTask) {
                case TASK:
                    appManager.addTask(Task.builder("Task")
                            .startTime(startTime)
                            .duration(duration)
                            .build());
                    break;
                case STORY:
                    final long epicId = (long) addTask[3];
                    appManager.addStory(Story.builder("Task", epicId)
                            .startTime(startTime)
                            .duration(duration)
                            .build());
                    break;
            }
        }
    }
}