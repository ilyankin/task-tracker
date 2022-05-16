package exceptions;

import models.tasks.AbstractTask;

import java.time.format.DateTimeFormatter;

public class ManagerIntersectionTaskIntervalsException extends RuntimeException {
    public ManagerIntersectionTaskIntervalsException(AbstractTask task1, AbstractTask task2) {
        super("The " + task1.getTypeTask() + " with id = " + task1.getId() + " has " +
                "startTime - " + task1.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                ", endTime - " + task1.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                " intersects with the " + task2.getTypeTask() + " with id = " + task2.getId() + " has " +
                "startTime - " + task2.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                ", endTime - " + task2.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
