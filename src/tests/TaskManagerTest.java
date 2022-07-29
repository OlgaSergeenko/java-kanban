package tests;

import exceptions.TaskManagerException;
import exceptions.TaskTimeValidationException;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class TaskManagerTest<T extends TaskManager> {

    public T taskManager;
    private Task task;
    private Epic epic;
    private Epic epic2;
    private Subtask subtask2;
    private Subtask subtask3;
    private Epic epic3;
    private Subtask subtask4;

    public abstract TaskManager createTaskManager();

    @BeforeEach
    public void updateTaskManager() {
        taskManager = (T) createTaskManager();
        task = new Task("Task1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 10, 10, 0), 30);
        epic = new Epic("Epic1", "Descr1", null, 0);
        epic2 = new Epic("Epic2", "Descr2", null, 0);
        subtask2 = new Subtask("Subtask2", "Descr2", Status.DONE,
                LocalDateTime.of(2022, 8, 20, 15, 30), 120, 3);
        subtask3 = new Subtask("Subtask3", "Descr3", Status.NEW,
                LocalDateTime.of(2022, 9, 12, 8, 45), 15, 3);
        epic3 = new Epic("Epic3", "Descr3", null, 0);
        subtask4 = new Subtask("Subtask4", "Descr4", Status.IN_PROGRESS,
                LocalDateTime.of(2022, 7, 9, 9, 0), 360, 6);
    }


    @Test
    void shouldCreateNewTask() {
        taskManager.createTask(task);
        final Task savedTask = taskManager.getTasks().get(0);
        Optional<Task> savedInPriorities = taskManager.getPrioritizedTasks().stream().findFirst();
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        savedInPriorities.ifPresent(value -> assertEquals(task, value, "Задача не найдена."));
    }

    @Test
    void shouldPrintTaskList() {
        taskManager.createTask(task);
        int taskId = task.getId();
        final List<Task> taskList = taskManager.getTasks();
        assertNotNull(taskList, "Задачи на возвращаются.");
        assertEquals(1, taskList.size(), "Неверное количество задач.");
        assertEquals(task, taskList.get(0), "Задачи не совпадают.");

    }

    @Test
    void shouldReturnEmptyTaskList_WhenNoTasks() {
        final List<Task> taskList = taskManager.getTasks();
        assertTrue(taskList.isEmpty(), "Список задач не пустой.");
    }

    @Test
    void shouldThrowNullPointerException_WhenEmptyTask() {
        final NullPointerException exception = assertThrows(NullPointerException.class,
                () -> taskManager.createTask(null));
        assertEquals(null, exception.getMessage());
    }

    @Test
    void shouldCreateNewEpic() {
        taskManager.createEpic(epic);
        final Epic savedEpic = taskManager.getEpics().get(0);
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
    }

    @Test
    void shouldThrowNullPointerException_WhenEmptyEpic() {
        final NullPointerException exception = assertThrows(NullPointerException.class,
                () -> taskManager.createEpic(null));
        assertEquals(null, exception.getMessage());
    }

    @Test
    void shouldPrintEpicList() {
        taskManager.createEpic(epic);
        final List<Epic> taskList = taskManager.getEpics();
        assertNotNull(taskList, "Задачи на возвращаются.");
        assertEquals(1, taskList.size(), "Неверное количество задач.");
        assertEquals(epic, taskList.get(0), "Задачи не совпадают.");
    }

    @Test
    void shouldReturnEmptyEpicList_WhenNoEpics() {
        final List<Epic> taskList = taskManager.getEpics();
        assertTrue(taskList.isEmpty(), "Список эпиков не пустой.");
    }

    @Test
    void shouldCreateNewSubtask() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        int id = subtask.getId();
        final Subtask savedSubtask = taskManager.getSubtasks().get(0);
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Integer> subtasks = epic.getSubtasks();
        assertNotNull(subtasks, "Подзадачи по конкретному эпику не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач в эпике.");
        assertEquals(subtask.getId(), subtasks.get(0), "Подзадачи не совпадают.");

        Optional<Task> savedInPriorities = taskManager.getPrioritizedTasks().stream().findFirst();
        savedInPriorities.ifPresent(value -> assertEquals(subtask, value, "Задача не найдена."));
    }

    @Test
    void shouldPrintSubtaskList() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        final List<Subtask> subtaskList = taskManager.getSubtasks();
        assertNotNull(subtaskList, "Подзадачи не возвращаются.");
        assertEquals(1, subtaskList.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtaskList.get(0), "Подзадачи не совпадают.");
    }

    @Test
    void shouldReturnEmptySubtaskList_WhenNoSubtasks() {
        final List<Subtask> subtaskList = taskManager.getSubtasks();
        assertTrue(subtaskList.isEmpty(), "Список подазадач не пустой.");
    }

    @Test
    void shouldThrowNullPointerException_WhenEmptySubtask() {
        final NullPointerException exception = assertThrows(NullPointerException.class,
                () -> taskManager.createSubtask(null));
        assertEquals(null, exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenSubtaskHasNoEpic() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
                    taskManager.createSubtask(subtask);
                });
        assertEquals("Для данной подзадачи еще не создан эпик.", exception.getMessage());
    }

    @Test
    void shouldComputeEpicStatus() {
        taskManager.createEpic(epic);
        Status status = epic.getStatus(); //пустой список подзадач
        assertEquals(Status.NEW, status, "Неверный статус при создании нового эпика.");

        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        status = epic.getStatus(); //все подзадачи со статусом NEW
        assertEquals(Status.NEW, status, "Неверный статус при добавлении подзадачи NEW.");

        taskManager.createEpic(epic2);
        taskManager.createSubtask(subtask2);
        status = epic2.getStatus(); //все подзадачи со статусом DONE
        assertEquals(Status.DONE, status, "Неверный статус при добавлении подзадачи  DONE.");

        taskManager.createSubtask(subtask3);
        status = epic2.getStatus(); //подзадачи со статусами NEW и DONE
        assertEquals(Status.IN_PROGRESS, status, "Неверный статус при добавлении подзадач NEW и DONE.");

        taskManager.createEpic(epic3);
        taskManager.createSubtask(subtask4);
        status = epic3.getStatus(); //все подзадачи со статусом IN PROGRESS
        assertEquals(Status.IN_PROGRESS, status, "Неверный статус при добавлении подзадачи IN PROGRESS.");
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask(task);
        taskManager.findTaskById(task.getId());
        taskManager.deleteAllTasks();
        final List<Task> taskList = taskManager.getTasks();
        assertTrue(taskList.isEmpty(), "Список задач не пустой.");

        List<Task> historyTaskList = taskManager.getHistory();
        assertTrue(historyTaskList.isEmpty(), "История задач не пустая.");

        Set<Task> tasks = taskManager.getPrioritizedTasks();
        assertTrue(tasks.isEmpty(), "Задачи не удалены из приоритетов.");
    }

    @Test
    void shouldDeleteAllEpics() {
        taskManager.createEpic(epic);
        taskManager.findEpicById(epic.getId());
        taskManager.deleteAllEpics();
        final List<Epic> epicList = taskManager.getEpics();
        assertTrue(epicList.isEmpty(), "Список эпиков не пустой.");

        List<Task> historyTaskList = taskManager.getHistory();
        assertTrue(historyTaskList.isEmpty(), "История задач не пустая.");
    }

    @Test
    void shouldDeleteAllSubtasks() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        taskManager.createEpic(epic2);
        taskManager.createSubtask(subtask2);
        taskManager.findEpicById(epic.getId());
        taskManager.findEpicById(epic2.getId());
        taskManager.findSubtaskById(subtask.getId());
        taskManager.findSubtaskById(subtask2.getId());

        taskManager.deleteAllSubtasks();

        final List<Epic> epicList = taskManager.getEpics();
        assertEquals(2, epicList.size(), "При удалении всех подзадач удаляются эпики.");
        final List<Subtask> subtaskList = taskManager.getSubtasks();
        assertTrue(subtaskList.isEmpty(), "Список подзадач не пустой.");
        final List<Integer> subtasksEpic1 = epic.getSubtasks();
        assertTrue(subtasksEpic1.isEmpty(), "Список подзадач у эпик1 не пустой.");
        final List<Integer> subtasksEpic2 = epic2.getSubtasks();
        assertTrue(subtasksEpic2.isEmpty(), "Список подзадач у эпик2 не пустой.");
        List<Task> historyTaskList = taskManager.getHistory();
        assertEquals(2, historyTaskList.size(), "Неверное количество задач в истории.");
    }

    @Test
    void shouldDeleteAllTaskTypes() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        taskManager.createTask(task);
        taskManager.findEpicById(epic.getId());
        taskManager.findTaskById(task.getId());
        taskManager.findSubtaskById(subtask.getId());

        taskManager.deleteAllTaskTypes();

        final List<Task> taskList = taskManager.getTasks();
        assertTrue(taskList.isEmpty(), "Список задач не пустой.");
        final List<Epic> epicList = taskManager.getEpics();
        assertTrue(epicList.isEmpty(), "Список эпиков не пустой.");
        final List<Subtask> subtaskList = taskManager.getSubtasks();
        assertTrue(subtaskList.isEmpty(), "Список подзадач не пустой.");
        final List<Integer> subtasksEpic1 = epic.getSubtasks();
        assertTrue(subtasksEpic1.isEmpty(), "Список подзадач у эпик не пустой.");
        List<Task> historyList = taskManager.getHistory();
        assertTrue(historyList.isEmpty(), "История просмотра не пустая при удалении всех типов задач.");
    }

    @Test
    void shouldFindTaskById() {
        taskManager.createTask(task);
        int taskIdToFind = 1;
        Task foundTask = taskManager.findTaskById(taskIdToFind);
        assertEquals(task, foundTask, "Вернулась неверная задача по id.");
    }

    @Test
    void shouldThrowTaskManagerException_WhenWrongTaskId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createTask(task);
                    int taskIdToFind = 2;
                    taskManager.findTaskById(taskIdToFind);
                });
        assertEquals("Задачи с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenTaskListIsEmpty() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int taskIdToFind = 1;
                    taskManager.findTaskById(taskIdToFind);
                });
        assertEquals("Список задач пустой.", exception.getMessage());
    }

    @Test
    void shouldFindEpicById() {
        taskManager.createEpic(epic);
        int epicIdToFind = 1;
        Task foundEpic = taskManager.findEpicById(epicIdToFind);
        assertEquals(epic, foundEpic, "Вернулся неверный эпик по id.");
    }

    @Test
    void shouldThrowTaskManagerException_WhenWrongEpicId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createEpic(epic);
                    int epicIdToFind = 2;
                    taskManager.findEpicById(epicIdToFind);
                });
        assertEquals("Эпика с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenEpicListIsEmpty() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int epicIdToFind = 3;
                    taskManager.findEpicById(epicIdToFind);
                });
        assertEquals("Список эпиков пустой.", exception.getMessage());
    }

    @Test
    void shouldFindSubtaskById() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        int subtaskIdToFind = 2;
        Task foundSubtask = taskManager.findSubtaskById(subtaskIdToFind);
        assertEquals(subtask, foundSubtask, "Вернулась неверная задача по id.");
    }

    @Test
    void shouldThrowTaskManagerException_WhenWrongSubtaskId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createEpic(epic);
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
                    taskManager.createSubtask(subtask);
                    int subtaskIdToFind = 3;
                    taskManager.findSubtaskById(subtaskIdToFind);
                });
        assertEquals("Подзадачи с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenSubtaskListIsEmpty() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int subtaskIdToFind = 3;
                    taskManager.findSubtaskById(subtaskIdToFind);
                });
        assertEquals("Список подзадач пустой.", exception.getMessage());
    }

    @Test
    void shouldUpdateTask() {
        taskManager.createTask(task);
        int id = task.getId();
        Task updatedTask = new Task("New name", "New descr", Status.IN_PROGRESS,
                LocalDateTime.of(2022, 8, 5, 15, 0), 35);
        updatedTask.setId(id);
        taskManager.updateTask(updatedTask);
        Task foundTask = taskManager.findTaskById(id);

        assertEquals(updatedTask, foundTask, "Задача не обновилась.");
    }

    @Test
    void shouldThrowTaskManagerException_WhenUpdatingEmptyTaskList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int taskToUpdateId = 1;
                    Task updatedTask = new Task("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022, 8, 5, 15, 0), 35);
                    taskManager.updateTask(updatedTask);
                });
        assertEquals("Список задач пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenUpdatingTaskWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createTask(task);
                    int taskToUpdateId = 5;
                    Task updatedTask = new Task("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022, 8, 5, 15, 0), 35);
                    taskManager.updateTask(updatedTask);
                });
        assertEquals("Задача для обновления не найдена по данному номеру.", exception.getMessage());
    }

    @Test
    void shouldUpdateEpic() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        int subtasksSize = epic.getSubtasks().size();
        int epicToUpdateId = epic.getId();
        Epic updatedEpic = new Epic("New name", "New descr", null, 0);
        updatedEpic.setId(epicToUpdateId);
        taskManager.updateEpic(updatedEpic);
        Epic foundEpic = taskManager.findEpicById(epicToUpdateId);
        int updatedSubtasksSize = foundEpic.getSubtasks().size();

        assertEquals(updatedEpic, foundEpic, "Эпик не обновился.");
        assertEquals(subtasksSize, updatedSubtasksSize, "При обновлении эпика подзадачи неверно подтягиваютсяю");
    }

    @Test
    void shouldThrowTaskManagerException_WhenUpdatingEmptyEpicList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int epicToUpdateId = 1;
                    Epic updatedEpic = new Epic("New name", "New descr", null, 0);
                    taskManager.updateEpic(updatedEpic);
                });
        assertEquals("Список эпиков пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenUpdatingEpicWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createEpic(epic);
                    int epicToUpdateId = 5;
                    Epic updatedEpic = new Epic("New name", "New descr", null, 0);
                    taskManager.updateEpic(updatedEpic);
                });
        assertEquals("Эпик для обновления не найден по данному номеру.", exception.getMessage());
    }

    @Test
    void shouldUpdateSubtask() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        int idSubtaskToUpdate = subtask.getId();
        Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                LocalDateTime.of(2022, 7, 15, 10, 20), 30, 1);
        updatedSubtask.setId(idSubtaskToUpdate);
        taskManager.updateSubtask(updatedSubtask);
        Subtask foundSubtask = taskManager.findSubtaskById(idSubtaskToUpdate);

        assertEquals(updatedSubtask, foundSubtask, "Подзадача не обновилась.");
        Status status = epic.getStatus();
        assertEquals(Status.IN_PROGRESS, status, "Статус эпика установлен неверно при обновлении подзадачи.");
    }

    @Test
    void shouldThrowTaskManagerException_WhenUpdatingEmptySubtaskList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
                    updatedSubtask.setId(1);
                    taskManager.updateSubtask(updatedSubtask);
                });
        assertEquals("Список подзадач пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenUpdatingSubtaskWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createEpic(epic);
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
                    taskManager.createSubtask(subtask);
                    Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022, 8, 25, 12, 30), 40, 1);
                    updatedSubtask.setId(5);
                    taskManager.updateSubtask(updatedSubtask);
                });
        assertEquals("Подзадача для обновления не найдена по данному номеру.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenUpdatingSubtaskWithWrongEpicId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createEpic(epic);
                    taskManager.createEpic(epic2);
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
                    taskManager.createSubtask(subtask);
                    Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022, 8, 25, 12, 30), 40, 5);
                    updatedSubtask.setId(3);
                    taskManager.updateSubtask(updatedSubtask);
                });
        assertEquals("Не найден эпик, содержащий подзадачу для обновления.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenUpdatingSubtaskOfAnotherEpic() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createEpic(epic);
                    taskManager.createEpic(epic2);
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
                    taskManager.createSubtask(subtask);
                    Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022, 8, 25, 12, 30), 40, 2);
                    updatedSubtask.setId(3);
                    taskManager.updateSubtask(updatedSubtask);
                });
        assertEquals("Данная подзадача не входит в указанный эпик.", exception.getMessage());
    }

    @Test
    void shouldDeleteTaskById() {
        taskManager.createTask(task);
        taskManager.findTaskById(task.getId());
        taskManager.deleteTaskById(task.getId());
        List<Task> taskList = taskManager.getTasks();
        List<Task> historyList = taskManager.getHistory();
        assertTrue(taskList.isEmpty(), "Задача по id не удалена.");
        assertTrue(historyList.isEmpty(), "Задача не удалена из истории при удалении по id.");
    }

    @Test
    void shouldThrowTaskManagerException_WhenDeleteTaskInEmptyTaskList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int taskToDeleteId = 1;
                    taskManager.deleteTaskById(taskToDeleteId);
                });
        assertEquals("Список задач пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenDeleteTaskWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createTask(task);
                    int taskToDeleteId = 5;
                    taskManager.deleteTaskById(taskToDeleteId);
                });
        assertEquals("Задачи с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldDeleteSubtaskById() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                LocalDateTime.of(2022, 8, 25, 12, 30), 40, 1);
        taskManager.createSubtask(subtask);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
        taskManager.findSubtaskById(subtask.getId());
        int subtaskToDeleteId = subtask.getId();
        taskManager.deleteSubtaskById(subtaskToDeleteId);
        Status newStatus = epic.getStatus();

        List<Subtask> subtaskList = taskManager.getSubtasks();
        List<Task> historyList = taskManager.getHistory();
        assertTrue(subtaskList.isEmpty(), "Подзадача по id не удалена.");
        assertTrue(historyList.isEmpty(), "Подзадача не удалена из истории при удалении по id.");
        assertEquals(Status.NEW, newStatus, "Неверное обновление статуса при удалении подзадачи.");
    }

    @Test
    void shouldThrowTaskManagerException_WhenDeleteSubtaskFromEmptySubtaskList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int subtaskToDeleteId = 1;
                    taskManager.deleteSubtaskById(subtaskToDeleteId);
                });
        assertEquals("Список подзадач пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenDeleteSubtaskWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createEpic(epic);
                    Subtask subtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022, 8, 25, 12, 30), 40, 1);
                    taskManager.createSubtask(subtask);
                    int subtaskToDeleteId = 5;
                    taskManager.deleteSubtaskById(subtaskToDeleteId);
                });
        assertEquals("подзадачи с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldDeleteEpicById() {
        taskManager.createEpic(epic);
        taskManager.findEpicById(epic.getId());
        taskManager.deleteEpicById(epic.getId());
        List<Epic> epicList = taskManager.getEpics();
        List<Task> historyList = taskManager.getHistory();
        assertTrue(epicList.isEmpty(), "Эпик по id не удален.");
        assertTrue(historyList.isEmpty(), "Эпик не удален из истории при удалении по id.");
    }

    @Test
    void shouldThrowTaskManagerException_WhenDeleteEpicFromEmptyEpicList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int epicToDeleteId = 1;
                    taskManager.deleteEpicById(epicToDeleteId);
                });
        assertEquals("Список эпиков пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenDeleteEpicWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    taskManager.createEpic(epic);
                    int epicToDeleteId = 5;
                    taskManager.deleteEpicById(epicToDeleteId);
                });
        assertEquals("Эпика с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenTasksWithSameStartTime() {
        final TaskTimeValidationException exception = assertThrows(TaskTimeValidationException.class,
                () -> {
                    taskManager.createTask(new Task("Task1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 10, 10, 0), 30));
                    taskManager.createTask(new Task("Task1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 10, 10, 0), 30));
                });
        assertEquals("Задача пересекается по времени.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerException_WhenTasksWithSameTime() {
        final TaskTimeValidationException exception = assertThrows(TaskTimeValidationException.class,
                () -> {
                    taskManager.createTask(new Task("Task1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 10, 10, 0), 30));
                    taskManager.createTask(new Task("Task1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022, 7, 10, 10, 0), 30));
                });
        assertEquals("Задача пересекается по времени.", exception.getMessage());
    }

    @Test
    void shouldGetPrioritizedTasks() {
        taskManager.createTask(task);
        taskManager.createTask(new Task("Task2", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 12, 31, 23, 0), 30));
        taskManager.createTask(new Task("Task3", "Descr1", Status.NEW,
                null, 30));
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 4);
        taskManager.createSubtask(subtask);
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        Optional<Task> firstTask = prioritizedTasks.stream().findFirst();

        firstTask.ifPresent(value -> assertEquals(task, value, "сортировка по приоритетам неверная(первая задача)"));
        assertEquals(4, prioritizedTasks.size(), "Неверное количество задач.");
    }

    @Test
    void shouldComputeEpicStartEndTimeAndEpicDuration() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 20, 10, 20), 30, 1);
        taskManager.createSubtask(subtask);
        Subtask subtask2 = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022, 7, 15, 15, 0), 150, 1);
        taskManager.createSubtask(subtask2);

        assertEquals(epic.getStartTime(), subtask2.getStartTime(), "Дата старта эпика рассчитана неверно.");
        assertEquals(epic.getEndTime(), subtask.getEndTime(), "Дата завершения эпика рассчитана неверно.");
        assertEquals(epic.getDuration(), (subtask.getDuration() + subtask2.getDuration()),
                "Продолжительность эпика рассчитана неверно.");
    }
}
