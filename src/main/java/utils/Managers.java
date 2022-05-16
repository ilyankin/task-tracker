package utils;


import managers.FileBackedTaskManager;
import managers.HTTPTaskManager;
import managers.InMemoryTaskManager;

import java.net.URI;
import java.nio.file.Path;

public class Managers {
    public static HTTPTaskManager getDefault() {
        return new HTTPTaskManager(URI.create("http://localhost:8078/"));
    }

    public static FileBackedTaskManager getFileBacked(Path path) {
        return FileBackedTaskManager.getInstance(path);
    }

    public static InMemoryTaskManager getInMemory() {
        return new InMemoryTaskManager();
    }
}
