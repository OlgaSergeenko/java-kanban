package tests;

import manager.FileBackedTasksManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    TaskManager fileBackedTasksManager;
    TaskManager newFileBackedTasksManager;
    File file = new File("taskFile.csv");
    Task task;
    Task task2;
    Epic epic;
    Subtask subtask;
    Subtask subtask2;

    @BeforeEach
    public void beforeEach() {
        fileBackedTasksManager = createTaskManager();
        file = new File("taskFile.csv");
        task = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,12,10,10,0), 30);
        task2 = new Task("Task2", "Descr2",Status.IN_PROGRESS,
                LocalDateTime.of(2022,7,10,10,0), 30);
        epic = new Epic("Epic1", "Descr1", null,0);
        subtask = new Subtask("Subtask", "Descr", Status.DONE,
                LocalDateTime.of(2022,8,20,15,30), 120, 3);
        subtask2 = new Subtask("Subtask2", "Descr2", Status.NEW,
                LocalDateTime.of(2022,8,20,15,30), 120, 3);
    }

    @Override
    public TaskManager createTaskManager() {
        return new FileBackedTasksManager();
    }

    @Test
    public void shouldSaveAndReadFromFile() {
        fileBackedTasksManager.createTask(task);
        fileBackedTasksManager.createTask(task2);
        fileBackedTasksManager.createEpic(epic);
        fileBackedTasksManager.createSubtask(subtask);
        fileBackedTasksManager.createSubtask(subtask2);
        fileBackedTasksManager.deleteTaskById(2);
        fileBackedTasksManager.deleteSubtaskById(4);
        fileBackedTasksManager.findTaskById(1);

        newFileBackedTasksManager = FileBackedTasksManager.loadFromFile(file);
        List<Task> taskList = newFileBackedTasksManager.getTaskList();
        assertEquals(1, taskList.size(), "Из файла восстановлено неверное количество задач.");
        assertEquals(task, taskList.get(0), "Задача из файла восстановлена неверно.");

        List<Epic> epicList = newFileBackedTasksManager.getEpicList();
        Epic loadedEpic = epicList.get(0);
        assertEquals(1, epicList.size(), "Из файла восстановлено неверное количество эпиков'.");
        assertEquals(epic, loadedEpic, "Эпик из файла восстановлен неверно.");

        List<Subtask> subtaskList = newFileBackedTasksManager.getSubtaskList();
        assertEquals(1, subtaskList.size(), "Из файла восстановлено неверное количество подзадач.");
        assertEquals(subtask2, subtaskList.get(0), "Подзадача из файла восстановлена неверно.");

        List<Task> historyList = newFileBackedTasksManager.getHistoryManager().getHistory();
        Task taskInHistory = historyList.get(0);
        assertEquals(1, historyList.size(), "Неверное количество задач в истории.");
        assertEquals(task, taskInHistory, "Задача в истории не совпадает.");
    }

    @Test
    public void shouldSaveAndReadFromFileWhenNoTasks() {
        fileBackedTasksManager.deleteAllTaskTypes();
        newFileBackedTasksManager = FileBackedTasksManager.loadFromFile(file);
        List<Task> taskList = newFileBackedTasksManager.getTaskList();
        assertEquals(0, taskList.size(), "Из файла восстановлено неверное количество задач.");

        List<Epic> epicList = newFileBackedTasksManager.getEpicList();
        assertEquals(0, epicList.size(), "Из файла восстановлено неверное количество эпиков'.");

        List<Subtask> subtaskList = newFileBackedTasksManager.getSubtaskList();
        assertEquals(0, subtaskList.size(), "Из файла восстановлено неверное количество подзадач.");

        List<Task> historyList = newFileBackedTasksManager.getHistoryManager().getHistory();
        assertEquals(0, historyList.size(), "Неверное количество задач в истории.");
    }

    @Test
    public void shouldSaveAndReadFromFileWithEmptyHistory() {
        fileBackedTasksManager.createTask(task);

        newFileBackedTasksManager = FileBackedTasksManager.loadFromFile(file);
        List<Task> historyList = newFileBackedTasksManager.getHistoryManager().getHistory();
        assertEquals(0, historyList.size(), "Неверное количество задач в истории.");
    }

    @Test
    public void shouldSaveAndReadFromFileWhenEpicWithNoSubtasks() {
        fileBackedTasksManager.createEpic(epic);

        newFileBackedTasksManager = FileBackedTasksManager.loadFromFile(file);
        List<Epic> epicList = newFileBackedTasksManager.getEpicList();
        Epic loadedEpic = epicList.get(0);
        List<Integer> subtasks = loadedEpic.getSubtasks();
        assertEquals(1, epicList.size(), "Из файла восстановлено неверное количество эпиков'.");
        assertEquals(epic, loadedEpic, "Эпик из файла восстановлен неверно.");
        assertEquals(0, subtasks.size(), "Из файла восстановлено неверное количество подзадач.");
    }
}