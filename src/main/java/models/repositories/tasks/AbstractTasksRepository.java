package models.repositories.tasks;

import models.tasks.AbstractTask;
import utils.TaskCounter;

public abstract class AbstractTasksRepository<T extends AbstractTask> implements ITasksRepository<T> {
    public final static TaskCounter TASK_COUNTER = TaskCounter.getInstance();

    protected AbstractTasksRepository() {
    }

    public abstract int size();
}
