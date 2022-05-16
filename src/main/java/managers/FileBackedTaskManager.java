package managers;

import managers.history.HistoryManager;
import models.enums.StateTask;
import models.enums.TypeTask;
import models.repositories.tasks.AbstractTasksRepository;
import models.repositories.tasks.CombinedTasksRepository;
import models.tasks.AbstractTask;
import models.tasks.Epic;
import models.tasks.Story;
import models.tasks.Task;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path path;
    protected CombinedTasksRepository combinedTasksRepository;

    private FileBackedTaskManager(Path path) {
        super();
        this.path = path;
    }

    protected FileBackedTaskManager() {
        super();
    }

    public static FileBackedTaskManager getInstance(Path path) {
        final FileBackedTaskManager fileBackedAppManager = new FileBackedTaskManager(path);
        fileBackedAppManager.load();
        return fileBackedAppManager;
    }

    public void save() {
        final String HEADER = "id,type,name,status,description,duration,startTime,epic";
        combinedTasksRepository = CombinedTasksRepository.getInstance(getEpicsRepository(), getTasksRepository());
        try (BufferedWriter fileWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            fileWriter.write(HEADER);
            fileWriter.write(System.lineSeparator());
            LinkedHashMap<Long, AbstractTask> tasks = combinedTasksRepository.getMapAbstractTasks();
            for (Map.Entry<Long, AbstractTask> task : tasks.entrySet()) {
                fileWriter.write(taskToString(task.getValue()));
            }
            fileWriter.write(System.lineSeparator());
            fileWriter.write(toString(super.historyManager));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileBackedTaskManager load() {
        FileBackedTaskManager appManager = new FileBackedTaskManager(this.path);
        List<String> lines;
        try {
            if (Files.notExists(path)) Files.createFile(path);
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (lines.size() > 1) {
            HashMap<Long, AbstractTask> newAbstractTasksByOldIds = new HashMap<>();
            for (int i = 1; i < lines.size() - 1; i++) {
                final String line = lines.get(i);
                if (line.isBlank()) break;
                final AbstractTask abstractTask = taskFromString(line, newAbstractTasksByOldIds);
                final Long abstractTaskId = abstractTask.getId();
                newAbstractTasksByOldIds.put(abstractTaskId, abstractTask);
                final TypeTask typeTask = abstractTask.getTypeTask();
                if (typeTask.isEpic()) {
                    newAbstractTasksByOldIds.put(abstractTaskId, appManager.addEpic((Epic) abstractTask));
                } else if (typeTask.isStory()) {
                    newAbstractTasksByOldIds.put(abstractTaskId, appManager.addStory((Story) abstractTask));
                } else {
                    newAbstractTasksByOldIds.put(abstractTaskId, appManager.addTask((Task) abstractTask));
                }
            }

            appManager.combinedTasksRepository = CombinedTasksRepository.getInstance(getEpicsRepository(), getTasksRepository());

            String historyLine = lines.get(lines.size() - 1);
            if (!historyLine.isBlank()) {
                historyFromString(appManager, historyLine, newAbstractTasksByOldIds);
            }
        }
        return appManager;
    }

    private void clearHistory() {
        getHistoryManager().clear();
    }

    @Override
    public <T extends AbstractTask> void createRepository(Collection<T> abstractTasks,
                                                          Class<? extends AbstractTasksRepository<T>> tasksRepositoryClass) {
        super.createRepository(abstractTasks, tasksRepositoryClass);
        save();
    }

    @Override
    public Task findTask(long id) {
        Task task = super.findTask(id);
        save();
        return task;
    }

    @Override
    public Task addTask(Task task) {
        Task result = super.addTask(task);
        save();
        return result;
    }

    @Override
    public Task updateTask(long id, Task task) {
        Task result = super.updateTask(id, task);
        save();
        return result;
    }

    @Override
    public Task deleteTask(long id) {
        Task task = super.deleteTask(id);
        save();
        return task;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic findEpic(long id) {
        Epic epic = super.findEpic(id);
        save();
        return epic;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic result = super.addEpic(epic);
        save();
        return result;
    }

    @Override
    public Epic updateEpic(long id, Epic epic) {
        Epic result = super.updateEpic(id, epic);
        save();
        return result;
    }

    @Override
    public Epic deleteEpic(long id) {
        Epic result = super.deleteEpic(id);
        save();
        return result;
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Story findStory(long id) {
        Story story = super.findStory(id);
        save();
        return story;
    }

    @Override
    public Story addStory(Story story) {
        Story result = super.addStory(story);
        save();
        return result;
    }

    @Override
    public Story updateStory(long id, Story story) {
        Story result = super.updateStory(id, story);
        save();
        return result;
    }

    @Override
    public Story deleteStory(long id) {
        Story result = super.deleteStory(id);
        save();
        return result;
    }

    @Override
    public void deleteAllStories(Epic epic) {
        super.deleteAllStories(epic);
        save();
    }

    private String toString(HistoryManager historyManager) {
        StringBuilder sb = new StringBuilder();

        if (!historyManager.getHistory().isEmpty()) {
            for (AbstractTask task : historyManager.getHistory()) {
                sb.append(task.getId()).append(",");
            }
        }

        return sb.toString();
    }

    private List<AbstractTask> historyFromString(FileBackedTaskManager fileBackedAppManager, String value,
                                                 HashMap<Long, AbstractTask> newAbstractTasksByOldIds) {
        String[] ids = value.split(",");
        if (ids.length != 0) {
            for (String id : ids) {
                fileBackedAppManager.historyManager.add(newAbstractTasksByOldIds.get(Long.parseLong(id)));
            }
        }

        return fileBackedAppManager.historyManager.getHistory();
    }


    private String taskToString(AbstractTask abstractTask) {
        StringBuilder sb = new StringBuilder();
        TypeTask typeTask;
        sb.append(abstractTask.getId())
                .append(",")
                .append(typeTask = abstractTask.getTypeTask())
                .append(",")
                .append(abstractTask.getName())
                .append(",")
                .append(abstractTask.getStateTask())
                .append(",")
                .append(abstractTask.getDescription())
                .append(",")
                .append(Optional.ofNullable(abstractTask.getDuration())
                        .map(Duration::toString)
                        .orElse(""))
                .append(",")
                .append(Optional.ofNullable(abstractTask.getStartTime())
                        .map(startTime -> startTime.format(ISO_LOCAL_DATE_TIME))
                        .orElse(""))
                .append(",");

        if (typeTask.isStory()) {
            Story story = (Story) abstractTask;
            sb.append(story.getEpicId());
        }
        return sb.append(System.lineSeparator()).toString();
    }

    private AbstractTask taskFromString(String value, HashMap<Long, AbstractTask> newAbstractTasksByOldIds) {
        String[] split = value.trim().split(",", 8);

        if (split.length > 8 || split.length < 4) throw new IllegalArgumentException();

        long id = Long.parseLong(split[0]);
        TypeTask typeTask = TypeTask.valueOf(split[1]);
        String name = split[2];
        StateTask stateTask = StateTask.valueOf(split[3]);
        String description = split[4];
        String duration = split[5];
        String startTime = split[6];

        if (typeTask.isEpic()) {
            return Epic.createEpic(id, name, stateTask, description,
                    duration.isEmpty() ? null : Duration.parse(duration),
                    startTime.isEmpty() ? null : LocalDateTime.parse(startTime, ISO_LOCAL_DATE_TIME));
        }

        if (typeTask.isStory()) {
            long epicId = Long.parseLong(split[7]);
            return Story.createStory(id, name, description,
                    newAbstractTasksByOldIds.get(epicId).getId(),
                    stateTask,
                    duration.isEmpty() ? null : Duration.parse(duration),
                    startTime.isEmpty() ? null : LocalDateTime.parse(startTime, ISO_LOCAL_DATE_TIME));
        }

        return Task.createTask(id, name, description, stateTask,
                duration.isEmpty() ? null : Duration.parse(duration),
                startTime.isEmpty() ? null : LocalDateTime.parse(startTime, ISO_LOCAL_DATE_TIME));
    }
}

