package models.tasks;

import models.enums.StateTask;
import models.enums.TypeTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Task extends AbstractTask {

    public static class Builder {
        private long id;
        private String name;
        private String description;
        private StateTask stateTask;
        private Duration duration;
        private LocalDateTime startTime;

        Builder(long id, String name) {
            this.id = id;
            this.name = Objects.requireNonNull(name, "name must not be null");
        }

        Builder(String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
        }

        Builder id(long id) {
            this.id = id;
            return this;
        }

        Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder stateTask(StateTask stateTask) {
            this.stateTask = stateTask;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private Task(Builder builder) {
        super(builder.id, builder.name, builder.description, TypeTask.TASK, builder.stateTask, builder.duration,
                builder.startTime);
    }

    public static Task createTask(long id, Task task) {
        return new Builder(id, task.name)
                .description(task.description)
                .stateTask(task.stateTask)
                .duration(task.duration)
                .startTime(task.startTime)
                .build();
    }

    public static Task createTask(String name, String description) {
        return createTask(0, name, description);
    }

    public static Task createTask(String name) {
        return createTask(name, "");
    }

    public static Task createTask(long id, String name, String description) {
        return new Builder(id, name).description(description).build();
    }

    public static Task createTask(long id, String name, String description, StateTask stateTask) {
        return new Builder(id, name)
                .description(description)
                .stateTask(stateTask)
                .build();
    }

    public static Task createTask(long id, String name, String description, StateTask stateTask,
                                  Duration duration, LocalDateTime startTime) {
        return new Builder(id, name)
                .description(description)
                .stateTask(stateTask)
                .duration(duration)
                .startTime(startTime)
                .build();
    }

    public Task setTask(Task task) {
        if (task != null) {
            setName(task.name);
            setDescription(task.description);
            setStateTask(task.stateTask);
            setDuration(task.duration);
            setStartTime(task.startTime);
        }
        return this;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public StateTask getStateTask() {
        return super.getStateTask();
    }

    public void setStateTask(StateTask stateTask) {
        this.stateTask = stateTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description.length='" + (description == null || description.isEmpty() ? 0
                : description.length()) + '\'' +
                ", stateTask=" + stateTask +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}
