package tests;

import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class TaskManagerTest<T extends TaskManager> {

    public T taskManager;

    public abstract TaskManager createTaskManager();

    @BeforeEach
    public void updateTaskManager() {
        taskManager = (T) createTaskManager();
    }
}
