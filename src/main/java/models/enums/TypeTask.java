package models.enums;


public enum TypeTask {
    EPIC,
    STORY,
    TASK;

    public boolean isEpic() {
        return EPIC == this;
    }

    public boolean isStory() {
        return STORY == this;
    }

    public boolean isTask() {
        return TASK == this;
    }
}
