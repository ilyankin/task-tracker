package models.repositories.tasks;

import models.tasks.Task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TasksRepository extends AbstractTasksRepository<Task> {

    private final Map<Long, Task> idTaskMap;

    public TasksRepository() {
        idTaskMap = new HashMap<>();
    }

    public TasksRepository(Collection<Task> tasks) {
        this();
        Objects.requireNonNull(tasks, "tasks must not be null");
        for (Task task : tasks) {
            idTaskMap.put(TASK_COUNTER.increment(), Task.createTask(TASK_COUNTER.getValue(), task));
        }
    }

    @Override
    public Collection<Task> findAll() {
        return idTaskMap.values();
    }

    @Override
    public Task find(long id) {
        return idTaskMap.get(id);
    }

    @Override
    public Task add(Task task) {
        final Task newTask;
        idTaskMap.put(TASK_COUNTER.increment(), newTask = Task.createTask(TASK_COUNTER.getValue(), task));
        return newTask;
    }

    @Override
    public Task update(long id, Task task) {
        final Task currentTask = idTaskMap.get(id);
        if (currentTask != null) {
            return currentTask.setTask(task);
        }
        return null;
    }

    @Override
    public Task delete(long id) {
        return idTaskMap.remove(id);
    }

    @Override
    public void clear() {
        idTaskMap.clear();
    }

    @Override
    public int size() {
        return idTaskMap.size();
    }

    public Map<Long, Task> getIdTaskMap() {
        return idTaskMap;
    }

    public boolean isEmpty() {
        return idTaskMap.isEmpty();
    }
}
