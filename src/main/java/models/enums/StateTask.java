package models.enums;

public enum StateTask {
    NEW("NEW_STATE", "This state describes a newly created entity"),
    IN_PROGRESS("IN_PROGRESS_STATE", "This state describes an entity in progress"),
    DONE("DONE_STATE", "This state describes an entity is done");

    private final String title;
    private final String description;

    StateTask(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
