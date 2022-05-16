package models.repositories.tasks;

import models.tasks.Epic;
import models.tasks.Story;

import java.util.*;

public class EpicsRepository extends AbstractTasksRepository<Epic> {

    private final Map<Long, Epic> idEpicMap;

    public EpicsRepository() {
        super();
        idEpicMap = new HashMap<>();
    }

    public EpicsRepository(Collection<Epic> epics) {
        this();
        Objects.requireNonNull(epics, "epics must not be null");
        for (Epic epic : epics) {
            idEpicMap.put(TASK_COUNTER.increment(), Epic.createEpic(TASK_COUNTER.getValue(), epic));
        }
    }

    @Override
    public Collection<Epic> findAll() {
        return idEpicMap.values();
    }

    @Override
    public Epic find(long id) {
        return idEpicMap.get(id);
    }

    @Override
    public Epic add(Epic epic) {
        final Epic newEpic;
        idEpicMap.put(TASK_COUNTER.increment(), newEpic = Epic.createEpic(TASK_COUNTER.getValue(), epic));
        return newEpic;
    }

    @Override
    public Epic update(long id, Epic newEpic) {
        Epic currentEpic = idEpicMap.get(id);
        if (currentEpic != null) {
            return currentEpic.setEpic(newEpic);
        }
        return null;
    }

    @Override
    public Epic delete(long id) {
        return idEpicMap.remove(id);
    }

    @Override
    public void clear() {
        idEpicMap.clear();
    }

    @Override
    public int size() {
        return idEpicMap.size();
    }

    public Collection<Story> findAllStories(Epic epic) {
        Epic mapEpic = idEpicMap.get(epic.getId());
        if (mapEpic == null) return Collections.emptyList();
        return epic.getStories();
    }

    public Story findStory(long id, Epic epic) {
        Epic mapEpic = idEpicMap.get(epic.getId());
        if (mapEpic == null) return null;
        return mapEpic.getStory(id);
    }

    public Story findStory(long id) {
        Story story;
        for (Epic epic : idEpicMap.values()) {
            if ((story = findStory(id, epic)) != null) return story;
        }
        return null;
    }

    public Story addStory(Story story, Epic epic) {
        final Epic mapEpic = idEpicMap.get(epic.getId());
        if (mapEpic == null) return null;
        return mapEpic.addStory(Story.createStory(TASK_COUNTER.increment(), story));
    }

    public Story updateStory(long id, Story story) {
        final Story mapStory = findStory(id);
        if (mapStory != null) {
            final long currentEpicId = mapStory.getEpicId();
            if (currentEpicId == story.getEpicId()) {
                return find(currentEpicId).updateStory(id, story);
            }
            deleteStory(id, currentEpicId);
            final Epic mapEpic = idEpicMap.get(story.getEpicId());
            if (mapEpic == null) return null;
            return mapEpic.addStory(Story.createStory(id, story));
        }
        return null;
    }

    public Story deleteStory(long id) {
        final Story story = findStory(id);
        if (story == null) return null;
        return deleteStory(id, story.getEpicId());
    }

    private Story deleteStory(long id, long epicId) {
        final Epic mapEpic = idEpicMap.get(epicId);
        if (mapEpic == null) return null;
        return mapEpic.removeStory(id);
    }

    public void clearStories(Epic epic) {
        idEpicMap.get(epic.getId()).removeAllStories();
    }

    public boolean isEmpty() {
        return idEpicMap.isEmpty();
    }

    Map<Long, Epic> getIdEpicMap() {
        return idEpicMap;
    }
}
