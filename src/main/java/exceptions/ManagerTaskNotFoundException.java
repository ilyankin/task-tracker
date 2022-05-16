package exceptions;

import models.enums.TypeTask;

public class ManagerTaskNotFoundException extends RuntimeException {
    private ManagerTaskNotFoundException(final String message) {
        super(message);
    }

    public ManagerTaskNotFoundException(final TypeTask typeTask, final long id) {
        this(typeTask + " was not found by id = " + id);
    }
}
