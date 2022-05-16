package models.repositories.tasks;

import models.tasks.AbstractTask;
import models.tasks.Epic;

import java.util.Collection;
import java.util.LinkedHashMap;

public class CombinedTasksRepository {
    private final LinkedHashMap<Long, AbstractTask> abstractTasks;

    private CombinedTasksRepository(EpicsRepository epicsRepository, TasksRepository tasksRepository) {
        abstractTasks = new LinkedHashMap<>(epicsRepository.size() + tasksRepository.size(), 0.75F, true);
        abstractTasks.putAll(tasksRepository.getIdTaskMap());
        abstractTasks.putAll(epicsRepository.getIdEpicMap());
        for (Epic epic : epicsRepository.findAll()) {
            abstractTasks.putAll(epic.getIdStoryMap());
        }
    }

    private CombinedTasksRepository(TasksRepository tasksRepository, EpicsRepository epicsRepository) {
        this(epicsRepository, tasksRepository);
    }

    private CombinedTasksRepository(EpicsRepository epicsRepository) {
        abstractTasks = new LinkedHashMap<>(epicsRepository.size(), 0.75F, true);
        abstractTasks.putAll(epicsRepository.getIdEpicMap());
        for (Epic epic : epicsRepository.findAll()) {
            abstractTasks.putAll(epic.getIdStoryMap());
        }
    }

    private CombinedTasksRepository(TasksRepository tasksRepository) {
        abstractTasks = new LinkedHashMap<>(tasksRepository.size(), 0.75F, true);
        abstractTasks.putAll(tasksRepository.getIdTaskMap());
    }

    public static CombinedTasksRepository getInstance(EpicsRepository epicsRepository,
                                                      TasksRepository tasksRepository) {
        if (epicsRepository == null || epicsRepository.isEmpty()) return new CombinedTasksRepository(tasksRepository);
        if (tasksRepository == null || tasksRepository.isEmpty()) return new CombinedTasksRepository(epicsRepository);
        return new CombinedTasksRepository(epicsRepository, tasksRepository);
    }

    public static CombinedTasksRepository getInstance(TasksRepository tasksRepository,
                                                      EpicsRepository epicsRepository) {
        return getInstance(epicsRepository, tasksRepository);
    }

    public LinkedHashMap<Long, AbstractTask> getMapAbstractTasks() {
        return abstractTasks;
    }

    public Collection<AbstractTask> getAbstractTasks() {
        return abstractTasks.values();
    }

    public void clear() {
        abstractTasks.clear();
    }
}
