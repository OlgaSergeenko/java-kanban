package tests;

import manager.InMemoryTaskManager;
import manager.TaskManager;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    public TaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}
