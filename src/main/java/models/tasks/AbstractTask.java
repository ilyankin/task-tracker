package models.tasks;

import models.enums.StateTask;
import models.enums.TypeTask;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class AbstractTask {
    protected final long id;
    protected String name;
    protected String description;
    protected TypeTask typeTask;
    protected StateTask stateTask;
    protected Duration duration;
    protected LocalDateTime startTime;

    protected AbstractTask(Long id, String name, String description, TypeTask typeTask,
                           StateTask stateTask, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.typeTask = typeTask;
        this.stateTask = stateTask == null ? StateTask.NEW : stateTask;
        this.duration = duration;
        this.startTime = startTime;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public StateTask getStateTask() {
        return stateTask;
    }

    public TypeTask getTypeTask() {
        return typeTask;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) return null;
        if (duration == null) return startTime;
        return startTime.plus(duration);
    }
}
