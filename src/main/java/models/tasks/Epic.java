package models.tasks;

import models.enums.StateTask;
import models.enums.TypeTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Epic extends AbstractTask {
    private Map<Long, Story> idStoryMap;

    public static class Builder {
        private long id;
        private String name;
        private String description;
        private StateTask stateTask;
        private Duration duration;
        private LocalDateTime startTime;

        public Builder(long id, String name) {
            this.id = id;
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

        Builder stateTask(StateTask stateTask) {
            this.stateTask = stateTask;
            return this;
        }

        Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Epic build() {
            return new Epic(this);
        }
    }

    private Epic(Builder builder) {
        super(builder.id, builder.name, builder.description, TypeTask.EPIC, builder.stateTask, builder.duration,
                builder.startTime);
        this.idStoryMap = new LinkedHashMap<>();
    }

    public static Epic createEpic(long id, Epic epic) {
        return createEpic(id, epic.name, epic.description, epic.getStories(), epic.duration, epic.startTime);
    }

    public static Epic createEpic(String name) {
        return createEpic(name, "");
    }

    public static Epic createEpic(String name, String description) {
        return new Builder(0, name).description(description).build();
    }

    public static Epic createEpic(long id, String name) {
        return new Builder(id, name).build();
    }

    public static Epic createEpic(long id, String name, String description) {
        return new Builder(id, name).description(description).build();
    }

    public static Epic createEpic(long id, String name, String description, Collection<Story> stories,
                                  Duration duration, LocalDateTime startTime) {
        Epic epic = new Builder(id, name)
                .description(description)
                .duration(duration)
                .startTime(startTime)
                .build();
        epic.setIdStoryMap(stories);
        return epic;
    }

    public static Epic createEpic(long id, String name, StateTask stateTask, String description,
                                  Duration duration, LocalDateTime startTime) {
        return new Builder(id, name)
                .stateTask(stateTask)
                .description(description)
                .duration(duration)
                .startTime(startTime)
                .build();
    }

    public static Epic createEpic(long id, String name, StateTask stateTask, String description) {
        return new Epic.Builder(id, name)
                .description(description)
                .stateTask(stateTask)
                .build();
    }

    public Story getStory(long id) {
        return idStoryMap.get(id);
    }

    public Story addStory(Story story) {
        if (this.id == story.getEpicId()) {
            idStoryMap.put(story.getId(), story);
            checkState();
        }
        return story;
    }

    public Story updateStory(long id, Story story) {
        return idStoryMap.get(id).setStory(story);
    }

    public Story removeStory(long id) {
        Story story = idStoryMap.remove(id);
        if (story != null) checkState();
        return story;
    }

    public void removeAllStories() {
        idStoryMap.clear();
        stateTask = StateTask.NEW;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Collection<Story> getStories() {
        return idStoryMap.values();
    }

    public Map<Long, Story> getIdStoryMap() {
        return idStoryMap;
    }

    public void setName(String name) {
        super.setName(name);
    }

    public void setDescription(String description) {
        super.setDescription(description);
    }

    @Override
    public Duration getDuration() {
        Duration epicDuration = Duration.ZERO;
        for (Story story : getStories()) {
            epicDuration = epicDuration.plus(story.duration == null ? Duration.ZERO : story.duration);
        }
        return Duration.ZERO.equals(epicDuration) ? null : epicDuration;
    }

    @Override
    public LocalDateTime getStartTime() {
        LocalDateTime minStoryStartTime = LocalDateTime.MAX;
        for (Story story : getStories()) {
            final LocalDateTime storyStartTime = story.getStartTime();
            if (storyStartTime != null && storyStartTime.isBefore(minStoryStartTime)) {
                minStoryStartTime = storyStartTime;
            }
        }
        return minStoryStartTime.equals(LocalDateTime.MAX) ? null : minStoryStartTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        if ((startTime = getStartTime()) == null) return null;
        if ((duration = getDuration()) == null) return startTime;
        LocalDateTime maxStoryEndTime = LocalDateTime.MIN;
        for (Story story : getStories()) {
            final LocalDateTime storyEndTime = story.getEndTime();
            if (storyEndTime.isAfter(maxStoryEndTime)) {
                maxStoryEndTime = storyEndTime;
            }
        }
        return maxStoryEndTime.equals(LocalDateTime.MIN) ? null : maxStoryEndTime;
    }

    public void setIdStoryMap(Collection<Story> stories) {
        int counterThisEpic = 0;

        for (Story story : stories) {
            if (id == story.getEpicId()) {
                counterThisEpic++;
            }
        }
        if (counterThisEpic == stories.size()) {
            this.idStoryMap = new LinkedHashMap<>();
            for (Story story : stories) {
                this.idStoryMap.put(story.getId(), story);
            }
            checkState();
        }
    }

    public Epic setEpic(Epic epic) {
        if (epic != null) {
            setName(epic.name);
            setDescription(epic.description);
            setIdStoryMap(epic.idStoryMap.values());
            setDuration(epic.duration);
            setStartTime(epic.startTime);
            checkState();
        }
        return this;
    }

    public void setStatusStory(long id, StateTask stateTask) {
        Story story = idStoryMap.get(id);
        if (id == story.getId()) {
            story.setStateTask(stateTask);
            checkState();
        }
    }

    private void checkState() {
        int counterNewStatusStories = 0;
        int counterDoneStatusStories = 0;
        for (Story story : idStoryMap.values()) {
            if (StateTask.NEW.equals(story.stateTask)) counterNewStatusStories++;
            if (StateTask.DONE.equals(story.stateTask)) counterDoneStatusStories++;
        }
        int storiesListLength = idStoryMap.size();
        if (counterNewStatusStories == storiesListLength) {
            stateTask = StateTask.NEW;
        } else if (counterDoneStatusStories == storiesListLength) {
            stateTask = StateTask.DONE;
        } else {
            stateTask = StateTask.IN_PROGRESS;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return id == epic.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description.length='" + (description == null || description.isEmpty() ? 0
                : description.length()) + '\'' +
                ", stateTask=" + stateTask +
                ", stories.size=" + idStoryMap.size() +
                ", duration=" + duration +
                ", startTime" + startTime +
                '}';
    }
}
