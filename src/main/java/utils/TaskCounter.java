package utils;

public class TaskCounter {
    private long counter;

    private TaskCounter() {
    }

    private static class HolderTaskCounter {
        private final static TaskCounter instance = new TaskCounter();
    }

    public static TaskCounter getInstance() {
        return HolderTaskCounter.instance;
    }

    public long getValue() {
        return counter;
    }

    public void setValue(long value) {
        counter = value;
    }

    public long increment() {
        return ++counter;
    }

    public long reset() {
        return counter = 0;
    }

}
