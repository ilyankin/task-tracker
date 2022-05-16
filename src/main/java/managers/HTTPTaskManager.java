package managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.enums.TypeTask;
import models.repositories.tasks.CombinedTasksRepository;
import models.taskSerializers.AbstractTaskDeserializer;
import models.tasks.AbstractTask;
import models.tasks.Epic;
import models.tasks.Story;
import models.tasks.Task;
import web.clients.KVTaskClient;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HTTPTaskManager extends FileBackedTaskManager {
    private final KVTaskClient client;
    private final String storageKey = "storage";
    private final String tasksKey = "tasks";
    private final String epicsKey = "epics";
    private final String storiesKey = "stories";
    private final String historyKey = "history";
    private final Gson gson = new Gson();

    public HTTPTaskManager(URI uriKVServer) {
        client = new KVTaskClient(uriKVServer);
        String emptyArray = "[]";
        client.put(storageKey, emptyArray);
        client.put(tasksKey, emptyArray);
        client.put(epicsKey, emptyArray);
        client.put(storiesKey, emptyArray);
        client.put(historyKey, emptyArray);
    }


    @Override
    public void save() {
        client.put(storageKey, gson.toJson(CombinedTasksRepository.getInstance(epicsRepository, tasksRepository).getAbstractTasks()));
        if (!tasksRepository.isEmpty()) {
            client.put(tasksKey, gson.toJson(findAllTasks()));
        }
        if (!epicsRepository.isEmpty()) {
            client.put(epicsKey, gson.toJson(findAllEpics()));
            client.put(storiesKey, gson.toJson(findAllStories()));
        }
        if (!historyManager.getHistory().isEmpty()) {
            client.put(historyKey, gson.toJson(super.historyManager.getHistory().stream().map(AbstractTask::getId).collect(Collectors.toList())));
        }
    }

    @Override
    public HTTPTaskManager load() {
        HTTPTaskManager taskManager = new HTTPTaskManager(client.getUrl());
        HashMap<Long, AbstractTask> newAbstractTasksByOldIds = new HashMap<>();
        loadStorage(taskManager, newAbstractTasksByOldIds);
        taskManager.combinedTasksRepository = CombinedTasksRepository.getInstance(getEpicsRepository(), getTasksRepository());
        loadHistory(taskManager, newAbstractTasksByOldIds);
        return taskManager;
    }

    private void loadStorage(HTTPTaskManager taskManager, HashMap<Long, AbstractTask> newAbstractTasksByOldIds) {
        AbstractTaskDeserializer deserializer = new AbstractTaskDeserializer("typeTask");
        deserializer.registerBarnType("EPIC", Epic.class);
        deserializer.registerBarnType("TASK", Task.class);
        deserializer.registerBarnType("STORY", Story.class);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(AbstractTask.class, deserializer)
                .create();
        List<AbstractTask> tasks = gson.fromJson(client.load(storageKey), new TypeToken<List<AbstractTask>>(){}.getType());
        if (tasks != null && !tasks.isEmpty()) {
            for (AbstractTask t : tasks) {
                Long abstractTaskId = t.getId();
                TypeTask typeTask = t.getTypeTask();
                newAbstractTasksByOldIds.put(abstractTaskId, t);
                if (typeTask.isEpic()) {
                    newAbstractTasksByOldIds.put(abstractTaskId, taskManager.addEpic((Epic) t));
                } else if (typeTask.isStory()) {
                    Story story = (Story) t;
                    story.setEpicId(newAbstractTasksByOldIds.get(((Story) t).getEpicId()).getId());
                    newAbstractTasksByOldIds.put(abstractTaskId, taskManager.addStory((Story) t));
                } else {
                    newAbstractTasksByOldIds.put(abstractTaskId, taskManager.addTask((Task) t));
                }
            }
        }
    }

    private void loadHistory(HTTPTaskManager taskManager, HashMap<Long, AbstractTask> newAbstractTasksByOldIds) {
        List<Long> history = gson.fromJson(client.load(historyKey), new TypeToken<List<Long>>(){}.getType());
        if (history != null && !history.isEmpty()) {
            history.forEach(id -> taskManager.historyManager.add(newAbstractTasksByOldIds.get(id)));
        }
    }
}
