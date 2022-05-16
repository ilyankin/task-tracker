package models.tasks;

import models.enums.StateTask;
import models.enums.TypeTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Story extends AbstractTask {
    private long epicId;

    public static class Builder {
        private long id;
        private final String name;
        private final long epicId;
        private String description;
        private StateTask stateTask;
        private Duration duration;
        private LocalDateTime startTime;

        Builder(String name, long epicId) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.epicId = epicId;
        }

        Builder(long id, String name, long epicId) {
            this.id = id;
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.epicId = epicId;
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

        public Story build() {
            return new Story(this);
        }
    }

    public static Builder builder(String name, long epicId) {
        return new Builder(name, epicId);
    }

    private Story(Builder builder) {
        super(builder.id, builder.name, builder.description, TypeTask.STORY, builder.stateTask, builder.duration,
                builder.startTime);
        ;
        this.epicId = builder.epicId;
    }

    public static Story createStory(String name, long epicId) {
        return new Builder(name, epicId).build();
    }

    public static Story createStory(String name, String description, long epicId) {
        return createStory(0, name, description, epicId);
    }

    public static Story createStory(long id, Story story) {
        return new Builder(id, story.name, story.epicId)
                .description(story.description)
                .stateTask(story.stateTask)
                .duration(story.duration)
                .startTime(story.startTime)
                .build();
    }

    public static Story createStory(long id, String name, long epicId) {
        return new Builder(id, name, epicId).build();
    }

    public static Story createStory(long id, String name, String description, long epicId) {
        return new Builder(id, name, epicId).description(description).build();
    }

    public static Story createStory(long id, String name, String description, long epicId, StateTask stateTask) {
        return new Builder(id, name, epicId)
                .description(description)
                .stateTask(stateTask)
                .build();
    }

    public static Story createStory(long id, String name, String description, long epicId, StateTask stateTask,
                                    Duration duration, LocalDateTime startTime) {
        return new Builder(id, name, epicId)
                .description(description)
                .stateTask(stateTask)
                .duration(duration)
                .startTime(startTime)
                .build();
    }


    public Story setStory(Story story) {
        if (story != null) {
            setName(story.name);
            setDescription(story.description);
            setEpicId(story.epicId);
            setDuration(story.duration);
            setStartTime(story.startTime);
        }
        return this;
    }

    public long getId() {
        return id;
    }

    public long getEpicId() {
        return epicId;
    }

    public void setEpicId(long epicId) {
        this.epicId = epicId;
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

    public StateTask getStateTask() {
        return stateTask;
    }

    public void setStateTask(StateTask stateTask) {
        this.stateTask = stateTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Story story = (Story) o;
        return id == story.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Story{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description.length='" + (description == null || description.isEmpty() ? 0
                : description.length()) + '\'' +
                ", stateTask=" + stateTask +
                ", epicId=" + epicId +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}
