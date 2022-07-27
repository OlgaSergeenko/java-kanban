package tests;

import HttpManager.HTTPTaskManager;
import HttpManager.HttpTaskServer;
import HttpManager.KVServer;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HTTPTaskManagerTest {

    @Test
    void loadFromServer() throws IOException {
        new KVServer().start();
        new HttpTaskServer().start();
        HTTPTaskManager manager = new HTTPTaskManager(new URL("http://localhost:8078"));
        Task task = new Task("Name1", "descr1", Status.NEW,
                LocalDateTime.of(2022,10,10,10,10,10),10);
        manager.createTask(task);
        Epic epic = new Epic("EpicName", "EpicDescr", null, 0);
        manager.createEpic(epic);
        Subtask subtask = new Subtask("SubName", "SubDescr", Status.IN_PROGRESS,
                LocalDateTime.of(2022,11,11,11,10), 30, 2);
        manager.createSubtask(subtask);
        manager.findTaskById(1);
        manager.findEpicById(2);
        HTTPTaskManager newManager = HTTPTaskManager.loadFromServer(new URL("http://localhost:8078"));
        assertEquals(2, newManager.getHistory().size(), "История восстановлена неверно.");

        newManager.createTask(new Task("name2", "des2", Status.NEW,
                LocalDateTime.of(2022,12,10,10,10,10),10));

        assertEquals(2, newManager.getTasks().size(), "Восстановлено неверное количество задач.");
        assertEquals(manager.findTaskById(1), newManager.findTaskById(1), "Задачи не совпадают.");

        assertEquals(1, newManager.getSubtasks().size(), "Восстановлено неверное количество подзадач.");
        assertEquals(manager.findSubtaskById(3), newManager.findSubtaskById(3),
                "Подзадачи не совпадают.");

        assertEquals(1, newManager.getEpics().size(), "Восстановлено неверное количество эпиков.");
        assertEquals(manager.findEpicById(2), newManager.findEpicById(2),
                "Эпики не совпадают.");
    }
}
