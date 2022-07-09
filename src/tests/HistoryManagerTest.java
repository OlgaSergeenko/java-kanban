package tests;

import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tasks.Status;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private static HistoryManager historyManager;
    private static InMemoryTaskManager inMemoryTaskManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        inMemoryTaskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldAddTaskToHistoryListAndReturnHistoryList() {
        Task task = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,12,10,10,0), 30);
        inMemoryTaskManager.createTask(task);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "Неверное количество задач в истории.");
    }

    @Test
    void shouldNotAddDuplicateTaskToHistoryList() {
        Task task = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,12,10,10,0), 30);
        inMemoryTaskManager.createTask(task);
        historyManager.add(task);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "повторная задача найдена.");
    }

    @Test
    void shouldRemoveTaskFromHistoryListById() {
        Task task = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,12,10,10,0), 30);
        inMemoryTaskManager.createTask(task);
        historyManager.add(task);
        historyManager.remove(1);
        final List<Task> history = historyManager.getHistory();
        boolean isTaskRemoved = history.contains(task);
        assertFalse(isTaskRemoved, "Задача не удалена.");
    }

    @Test
    void shouldRemoveTaskFromHistoryListFromStartMiddleEnd() {
        Task task = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,8,5,10,0), 60);
        Task task2 = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,9,6,12,15), 30);
        Task task3 = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,10,7,15,30), 600);
        Task task4 = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,11,8,22,25), 60);
        Task task5 = new Task("Task1", "Descr1",Status.NEW,
                LocalDateTime.of(2022,12,10,9,0), 15);
        inMemoryTaskManager.createTask(task);
        inMemoryTaskManager.createTask(task2);
        inMemoryTaskManager.createTask(task3);
        inMemoryTaskManager.createTask(task4);
        inMemoryTaskManager.createTask(task5);
        historyManager.add(task);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task4);
        historyManager.add(task5);
        historyManager.remove(1); //удаление из начала списка
        Task newFirst = historyManager.getHistory().get(0);
        assertEquals(task2, newFirst, "Неверное смещение при удалении первой задачи.");
        historyManager.remove(5); //удаление из конца списка
        int lastIndex = historyManager.getHistory().size() - 1;
        Task newLast = historyManager.getHistory().get(lastIndex);
        assertEquals(task4, newLast, "Неверно определена последняя задача.");
        int index = historyManager.getHistory().indexOf(task3);
        Task preTask = historyManager.getHistory().get(index - 1);
        Task postTask = historyManager.getHistory().get(index + 1);
        historyManager.remove(3); //удаление из середины списка
        assertEquals(preTask, task2, "Предшествующая задача восстановлена неверно.");
        assertEquals(postTask, task4, "Следующая задача восстановлена неверно.");
    }

    @Test
    void shouldClearHistoryList() {
        historyManager.clear();
        boolean isHistoryListEmpty = historyManager.getHistory().isEmpty();
        assertTrue(isHistoryListEmpty, "История задач не пустая.");
    }

    @Test
    void shouldReturnNullWhenHistoryListIsEmpty() {
        int historyListSize = historyManager.getHistory().size();
        assertEquals(0, historyListSize, "История задач не пустая.");
    }
}