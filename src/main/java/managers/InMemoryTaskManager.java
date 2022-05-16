package managers;

import exceptions.ManagerIntersectionTaskIntervalsException;
import exceptions.ManagerTaskNotFoundException;
import managers.history.InMemoryHistoryManager;
import models.enums.TypeTask;
import models.repositories.tasks.AbstractTasksRepository;
import models.repositories.tasks.EpicsRepository;
import models.repositories.tasks.TasksRepository;
import models.tasks.AbstractTask;
import models.tasks.Epic;
import models.tasks.Story;
import models.tasks.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected TasksRepository tasksRepository;
    protected EpicsRepository epicsRepository;
    protected InMemoryHistoryManager historyManager;
    protected TreeSet<AbstractTask> tasksSortedByStartTime;

    public InMemoryTaskManager() {
        tasksRepository = new TasksRepository();
        epicsRepository = new EpicsRepository();
        historyManager = new InMemoryHistoryManager();
        tasksSortedByStartTime = new TreeSet<>(
                Comparator.comparing(AbstractTask::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingLong(AbstractTask::getId)
        );
    }

    public Collection<Task> findAllTasks() {
        return tasksRepository.findAll();
    }

    @Override
    public Task findTask(long id) {
        final Task task = tasksRepository.find(id);
        if (task == null) throw new ManagerTaskNotFoundException(TypeTask.TASK, id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Task addTask(Task task) {
        AbstractTask intersectTask = checkIntersection(task);
        if (intersectTask != null) throw new ManagerIntersectionTaskIntervalsException(task, intersectTask);
        final Task result = tasksRepository.add(task);
        addToPrioritizedListTasks(result);
        return result;
    }

    @Override
    public Task updateTask(long id, Task task) {
        AbstractTask intersectTask = checkIntersection(task);
        if (intersectTask != null) throw new ManagerIntersectionTaskIntervalsException(task, intersectTask);
        final Task result = tasksRepository.update(id, task);
        if (result == null) throw new ManagerTaskNotFoundException(TypeTask.TASK, id);
        return result;
    }

    @Override
    public Task deleteTask(long id) {
        final Task task = tasksRepository.delete(id);
        if (task == null) throw new ManagerTaskNotFoundException(TypeTask.TASK, id);
        historyManager.remove(id);
        tasksSortedByStartTime.remove(task);
        return task;
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasksRepository.findAll()) {
            historyManager.remove(task.getId());
            tasksSortedByStartTime.remove(task);
        }
        tasksRepository.clear();
    }

    @Override
    public Collection<Epic> findAllEpics() {
        return epicsRepository.findAll();
    }

    @Override
    public Epic findEpic(long id) {
        Epic epic = epicsRepository.find(id);
        if (epic == null) throw new ManagerTaskNotFoundException(TypeTask.EPIC, id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Epic addEpic(Epic epic) {
        return epicsRepository.add(epic);
    }

    @Override
    public Epic updateEpic(long id, Epic epic) {
        AbstractTask intersectEpic = checkIntersection(epic);
        if (intersectEpic != null) throw new ManagerIntersectionTaskIntervalsException(epic, intersectEpic);
        final Epic result = epicsRepository.update(id, epic);
        if (result == null) throw new ManagerTaskNotFoundException(TypeTask.EPIC, id);
        return result;
    }

    @Override
    public Epic deleteEpic(long id) {
        final Epic epic = epicsRepository.delete(id);
        if (epic == null) throw new ManagerTaskNotFoundException(TypeTask.EPIC, id);
        for (Story story : epic.getStories()) {
            tasksSortedByStartTime.remove(story);
            historyManager.remove(id);
        }
        historyManager.remove(id);
        return epic;
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epicsRepository.findAll()) {
            for (Story story : epic.getStories()) {
                historyManager.remove(story.getId());
                tasksSortedByStartTime.remove(story);
            }
            historyManager.remove(epic.getId());
        }
        epicsRepository.clear();
    }

    @Override
    public Collection<Story> findAllStories(long epicId) {
        return epicsRepository.find(epicId).getStories();
    }

    public Collection<Story> findAllStories() {
        List<Story> stories = new ArrayList<>();
        for (Epic epic : epicsRepository.findAll()) {
            stories.addAll(epic.getStories());
        }
        return stories;
    }
    @Override
    public Story findStory(long id) {
        Story story = epicsRepository.findStory(id);
        if (story == null) throw new ManagerTaskNotFoundException(TypeTask.STORY, id);
        historyManager.add(story);
        return story;
    }

    @Override
    public Story addStory(Story story) {
        AbstractTask intersectStory = checkIntersection(story);
        if (intersectStory != null) throw new ManagerIntersectionTaskIntervalsException(story, intersectStory);
        final Story result = epicsRepository.addStory(story, epicsRepository.find(story.getEpicId()));
        addToPrioritizedListStories(result);
        return result;
    }

    @Override
    public Story updateStory(long id, Story story) {
        AbstractTask intersectStory = checkIntersection(story);
        if (intersectStory != null) throw new ManagerIntersectionTaskIntervalsException(story, intersectStory);
        final Story result = epicsRepository.updateStory(id, story);
        if (result == null) throw new ManagerTaskNotFoundException(TypeTask.STORY, id);
        return result;
    }

    @Override
    public Story deleteStory(long id) {
        final Story story = epicsRepository.deleteStory(id);
        if (story == null) throw new ManagerTaskNotFoundException(TypeTask.STORY, id);
        tasksSortedByStartTime.remove(story);
        historyManager.remove(id);
        return story;
    }

    @Override
    public void deleteAllStories(Epic epic) {
        for (Story story : epic.getStories()) {
            historyManager.remove(story.getId());
            tasksSortedByStartTime.remove(story);
        }
        epicsRepository.clearStories(epic);
    }

    @Override
    public <T extends AbstractTask> void createRepository(Collection<T> abstractTasks,
                                                          Class<? extends AbstractTasksRepository<T>> tasksRepositoryClass) {
        if (EpicsRepository.class.equals(tasksRepositoryClass)) {
            epicsRepository = new EpicsRepository();
            for (T epic : abstractTasks) addEpic((Epic) epic);
        } else if (TasksRepository.class.equals(tasksRepositoryClass)) {
            tasksRepository = new TasksRepository();
            for (T task : abstractTasks) addTask((Task) task);
        } else {
            throw new IllegalArgumentException("Репозиотрия с таким именем класса " + tasksRepositoryClass + " не существует");
        }
    }

    @Override
    public long size() {
        long numberAllStories = 0;
        for (Epic epic : epicsRepository.findAll()) {
            numberAllStories += epic.getStories().size();
        }
        return epicsRepository.size() + numberAllStories + tasksRepository.size();
    }

    public List<AbstractTask> getPrioritizedTasks() {
        return new ArrayList<>(tasksSortedByStartTime);
    }


    private void addToPrioritizedListTasks(Task task) {
        tasksSortedByStartTime.add(task);
    }

    private void addToPrioritizedListStories(Story story) {
        tasksSortedByStartTime.add(story);
    }

    private AbstractTask checkIntersection(AbstractTask checkedTask) {
        final TreeSet<AbstractTask> prioritizedTasks = tasksSortedByStartTime;
        if (prioritizedTasks.size() > 0) {
            final LocalDateTime startTimeCheckedTask = checkedTask.getStartTime();
            final LocalDateTime endTimeCheckedTask = checkedTask.getEndTime();
            if (startTimeCheckedTask != null) {
                for (AbstractTask prioritizedTask : prioritizedTasks) {
                    final LocalDateTime startTimePrioritizedTask = prioritizedTask.getStartTime();
                    final LocalDateTime endTimePrioritizedTask = prioritizedTask.getEndTime();
                    if (startTimePrioritizedTask != null && intersected(startTimeCheckedTask, endTimeCheckedTask,
                            startTimePrioritizedTask, endTimePrioritizedTask)) {
                        return prioritizedTask;
                    }
                }
            }
        }
        return null;
    }

    private boolean intersected(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        if (start1.isAfter(start2) && start1.isBefore(end2)) {
            return true;
        }
        return start2.isAfter(start1) && start2.isBefore(end1);
    }

    public TasksRepository getTasksRepository() {
        return tasksRepository;
    }

    public EpicsRepository getEpicsRepository() {
        return epicsRepository;
    }

    public InMemoryHistoryManager getHistoryManager() {
        return historyManager;
    }
}
