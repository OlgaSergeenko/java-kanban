package tests;

import exceptions.TaskManagerException;
import manager.InMemoryTaskManager;
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

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    TaskManager inMemoryTaskManager;
    Task task;
    Epic epic;
    Epic epic2;
    Subtask subtask2;
    Subtask subtask3;
    Epic epic3;
    Subtask subtask4;

    @Override
    public TaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @BeforeEach
    public void beforeEach() {
        inMemoryTaskManager = createTaskManager();
        task = new Task("Task1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,10,10,0), 30);
        epic = new Epic("Epic1", "Descr1", null,0);
        epic2 = new Epic("Epic2", "Descr2",null,0);
        subtask2 = new Subtask("Subtask2", "Descr2", Status.DONE,
                LocalDateTime.of(2022,8,20,15,30), 120, 3);
        subtask3 = new Subtask("Subtask3", "Descr3", Status.NEW,
                LocalDateTime.of(2022,9,12,8,45), 15, 3);
        epic3 = new Epic("Epic3", "Descr3", null, 0);
        subtask4 = new Subtask("Subtask4", "Descr4", Status.IN_PROGRESS,
                LocalDateTime.of(2022,7,9,9,0), 360,6);
    }

    @Test
    void shouldCreateNewTask() {
        inMemoryTaskManager.createTask(task);
        int taskId = task.getId();
        final Task savedTask = inMemoryTaskManager.printTaskList().get(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    void shouldPrintTaskList() {
        inMemoryTaskManager.createTask(task);
        int taskId = task.getId();
        final Map<Integer, Task> taskList = inMemoryTaskManager.printTaskList();
        assertNotNull(taskList, "Задачи на возвращаются.");
        assertEquals(1, taskList.size(), "Неверное количество задач.");
        assertEquals(task, taskList.get(taskId), "Задачи не совпадают.");
    }

    @Test
    void shouldReturnEmptyTaskListWhenNoTasks() {
        final Map<Integer, Task> taskList = inMemoryTaskManager.printTaskList();
        assertTrue(taskList.isEmpty(), "Список задач не пустой.");
        assertEquals(0, taskList.size(), "Неверное количество задач.");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenEmptyTask() {
        final NullPointerException exception = assertThrows(NullPointerException.class,
                () -> inMemoryTaskManager.createTask(null));
        assertNull(exception.getMessage());
    }

    @Test
    void shouldCreateNewEpic() {
        inMemoryTaskManager.createEpic(epic);
        int epicId = epic.getId();
        final Epic savedEpic = inMemoryTaskManager.printEpicList().get(epicId);
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenEmptyEpic() {
        final NullPointerException exception = assertThrows(NullPointerException.class,
                () -> inMemoryTaskManager.createEpic(null));
        assertNull(exception.getMessage());
    }

    @Test
    void shouldPrintEpicList() {
        inMemoryTaskManager.createEpic(epic);
        int epicId = epic.getId();
        final Map<Integer, Epic> taskList = inMemoryTaskManager.printEpicList();
        assertNotNull(taskList, "Задачи на возвращаются.");
        assertEquals(1, taskList.size(), "Неверное количество задач.");
        assertEquals(epic, taskList.get(epicId), "Задачи не совпадают.");
    }

    @Test
    void shouldReturnEmptyEpicListWhenNoEpics() {
        final Map<Integer, Epic> taskList = inMemoryTaskManager.printEpicList();
        assertTrue(taskList.isEmpty(), "Список эпиков не пустой.");
        assertEquals(0, taskList.size(), "Неверное количество эпиков.");
    }

    @Test
    void shouldCreateNewSubtask() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        int id = subtask.getId();
        final Subtask savedSubtask = inMemoryTaskManager.printSubtaskList().get(id);
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final ArrayList<Integer> subtasks = epic.getSubtasks();
        assertNotNull(subtasks, "Подзадачи по конкретному эпику не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач в эпике.");
        assertEquals(subtask.getId(), subtasks.get(0), "Подзадачи не совпадают.");
    }

    @Test
    void shouldPrintSubtaskList() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        int id = subtask.getId();
        final Map<Integer, Subtask> subtaskList = inMemoryTaskManager.printSubtaskList();
        assertNotNull(subtaskList, "Подзадачи не возвращаются.");
        assertEquals(1, subtaskList.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtaskList.get(id), "Подзадачи не совпадают.");
    }

    @Test
    void shouldReturnEmptySubtaskListWhenNoSubtasks() {
        final Map<Integer, Subtask> subtaskList = inMemoryTaskManager.printSubtaskList();
        assertTrue(subtaskList.isEmpty(), "Список подазадач не пустой.");
        assertEquals(0, subtaskList.size(), "Неверное количество подзадач.");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenEmptySubtask() {
        final NullPointerException exception = assertThrows(NullPointerException.class,
                () -> inMemoryTaskManager.createSubtask(null));
        assertNull(exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenSubtaskHasNoEpic() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,20,10,20), 30, 1);
                    inMemoryTaskManager.createSubtask(subtask);
                });
        assertEquals("Для данной подзадачи еще не создан эпик.", exception.getMessage());
    }

    @Test
    void shouldComputeEpicStatus() {
        inMemoryTaskManager.createEpic(epic);
        Status status = epic.getStatus(); //пустой список подзадач
        assertEquals(Status.NEW, status, "Неверный статус при создании нового эпика.");

        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        status = epic.getStatus(); //все подзадачи со статусом NEW
        assertEquals(Status.NEW, status, "Неверный статус при добавлении подзадачи NEW.");

        inMemoryTaskManager.createEpic(epic2);
        inMemoryTaskManager.createSubtask(subtask2);
        status = epic2.getStatus(); //все подзадачи со статусом DONE
        assertEquals(Status.DONE, status, "Неверный статус при добавлении подзадачи  DONE.");

        inMemoryTaskManager.createSubtask(subtask3);
        status = epic2.getStatus(); //подзадачи со статусами NEW и DONE
        assertEquals(Status.IN_PROGRESS, status, "Неверный статус при добавлении подзадач NEW и DONE.");

        inMemoryTaskManager.createEpic(epic3);
        inMemoryTaskManager.createSubtask(subtask4);
        status = epic3.getStatus(); //все подзадачи со статусом IN PROGRESS
        assertEquals(Status.IN_PROGRESS, status, "Неверный статус при добавлении подзадачи IN PROGRESS.");
    }

    @Test
    void shouldDeleteAllTasks() {
        inMemoryTaskManager.createTask(task);
        inMemoryTaskManager.findTaskById(task.getId());
        inMemoryTaskManager.deleteAllTasks();
        final Map<Integer, Task> taskList = inMemoryTaskManager.printTaskList();
        assertTrue(taskList.isEmpty(), "Список задач не пустой.");
        assertEquals(0, taskList.size(), "Неверное количество задач.");

        List<Task> historyTaskList = inMemoryTaskManager.getHistoryManager().getHistory();
        assertTrue(historyTaskList.isEmpty(), "История задач не пустая.");
        assertEquals(0, historyTaskList.size(), "Неверное количество задач.");
    }

    @Test
    void shouldDeleteAllEpics() {
        inMemoryTaskManager.createEpic(epic);
        inMemoryTaskManager.findEpicById(epic.getId());
        inMemoryTaskManager.deleteAllEpics();
        final Map<Integer, Epic> epicList = inMemoryTaskManager.printEpicList();
        assertTrue(epicList.isEmpty(), "Список эпиков не пустой.");
        assertEquals(0, epicList.size(), "Неверное количество эпиков.");

        List<Task> historyTaskList = inMemoryTaskManager.getHistoryManager().getHistory();
        assertTrue(historyTaskList.isEmpty(), "История задач не пустая.");
        assertEquals(0, historyTaskList.size(), "Неверное количество задач.");
    }

    @Test
    void shouldDeleteAllSubtasks() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        inMemoryTaskManager.createEpic(epic2);
        inMemoryTaskManager.createSubtask(subtask2);
        inMemoryTaskManager.findEpicById(epic.getId());
        inMemoryTaskManager.findEpicById(epic2.getId());
        inMemoryTaskManager.findSubtaskById(subtask.getId());
        inMemoryTaskManager.findSubtaskById(subtask2.getId());

        inMemoryTaskManager.deleteAllSubtasks();

        final Map<Integer, Epic> epicList = inMemoryTaskManager.printEpicList();
        assertEquals(2, epicList.size(), "При удалении всех подзадач удаляются эпики.");
        final Map<Integer, Subtask> subtaskList = inMemoryTaskManager.printSubtaskList();
        assertTrue(subtaskList.isEmpty(), "Список подзадач не пустой.");
        final List<Integer> subtasksEpic1 = epic.getSubtasks();
        assertTrue(subtasksEpic1.isEmpty(), "Список подзадач у эпик1 не пустой.");
        final List<Integer> subtasksEpic2 = epic2.getSubtasks();
        assertTrue(subtasksEpic2.isEmpty(), "Список подзадач у эпик2 не пустой.");
        List<Task> historyTaskList = inMemoryTaskManager.getHistoryManager().getHistory();
        assertEquals(2, historyTaskList.size(), "Неверное количество задач в истории.");
    }

    @Test
    void shouldDeleteAllTaskTypes() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        inMemoryTaskManager.createTask(task);
        inMemoryTaskManager.findEpicById(epic.getId());
        inMemoryTaskManager.findTaskById(task.getId());
        inMemoryTaskManager.findSubtaskById(subtask.getId());

        inMemoryTaskManager.deleteAllTaskTypes();

        final Map<Integer, Task> taskList = inMemoryTaskManager.printTaskList();
        assertTrue(taskList.isEmpty(), "Список задач не пустой.");
        final Map<Integer, Epic> epicList = inMemoryTaskManager.printEpicList();
        assertTrue(epicList.isEmpty(), "Список эпиков не пустой.");
        final Map<Integer, Subtask> subtaskList = inMemoryTaskManager.printSubtaskList();
        assertTrue(subtaskList.isEmpty(), "Список подзадач не пустой.");
        final List<Integer> subtasksEpic1 = epic.getSubtasks();
        assertTrue(subtasksEpic1.isEmpty(), "Список подзадач у эпик не пустой.");
        List<Task> historyList = inMemoryTaskManager.getHistoryManager().getHistory();
        assertTrue(historyList.isEmpty(), "История просмотра не пустая при удалении всех типов задач.");
    }

    @Test
    void shouldFindTaskById() {
        inMemoryTaskManager.createTask(task);
        int taskIdToFind = 1;
        Task foundTask = inMemoryTaskManager.findTaskById(taskIdToFind);
        assertEquals(task, foundTask, "Вернулась неверная задача по id.");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenWrongTaskId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createTask(task);
                    int taskIdToFind = 2;
                    inMemoryTaskManager.findTaskById(taskIdToFind);
                });
        assertEquals("Задачи с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenTaskListIsEmpty() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int taskIdToFind = 1;
                    inMemoryTaskManager.findTaskById(taskIdToFind);
                });
        assertEquals("Список задач пустой.", exception.getMessage());
    }

    @Test
    void shouldFindEpicById() {
        inMemoryTaskManager.createEpic(epic);
        int epicIdToFind = 1;
        Task foundEpic = inMemoryTaskManager.findEpicById(epicIdToFind);
        assertEquals(epic, foundEpic, "Вернулся неверный эпик по id.");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenWrongEpicId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createEpic(epic);
                    int epicIdToFind = 2;
                    inMemoryTaskManager.findEpicById(epicIdToFind);
                });
        assertEquals("Эпика с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenEpicListIsEmpty() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int epicIdToFind = 3;
                    inMemoryTaskManager.findEpicById(epicIdToFind);
                });
        assertEquals("Список эпиков пустой.", exception.getMessage());
    }

    @Test
    void shouldFindSubtaskById() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        int subtaskIdToFind = 2;
        Task foundSubtask = inMemoryTaskManager.findSubtaskById(subtaskIdToFind);
        assertEquals(subtask, foundSubtask, "Вернулась неверная задача по id.");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenWrongSubtaskId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createEpic(epic);
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,20,10,20), 30, 1);
                    inMemoryTaskManager.createSubtask(subtask);
                    int subtaskIdToFind = 3;
                    inMemoryTaskManager.findSubtaskById(subtaskIdToFind);
                });
        assertEquals("Подзадачи с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenSubtaskListIsEmpty() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int subtaskIdToFind = 3;
                    inMemoryTaskManager.findSubtaskById(subtaskIdToFind);
                });
        assertEquals("Список подзадач пустой.", exception.getMessage());
    }

    @Test
    void shouldUpdateTask() {
        inMemoryTaskManager.createTask(task);
        int taskToUpdateId = task.getId();
        Task updatedTask = new Task("New name", "New descr", Status.IN_PROGRESS,
                LocalDateTime.of(2022,8,5,15,0), 35);
        inMemoryTaskManager.updateTask(updatedTask, taskToUpdateId);
        Task foundTask = inMemoryTaskManager.findTaskById(taskToUpdateId);

        assertEquals(updatedTask, foundTask, "Задача не обновилась.");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenUpdatingEmptyTaskList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int taskToUpdateId = 1;
                    Task updatedTask = new Task("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022,8,5,15,0), 35);
                    inMemoryTaskManager.updateTask(updatedTask, taskToUpdateId);
                });
        assertEquals("Список задач пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenUpdatingTaskWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createTask(task);
                    int taskToUpdateId = 5;
                    Task updatedTask = new Task("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022,8,5,15,0), 35);
                    inMemoryTaskManager.updateTask(updatedTask, taskToUpdateId);
                });
        assertEquals("Задача для обновления не найдена по данному номеру.", exception.getMessage());
    }

    @Test
    void shouldUpdateEpic() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        int subtasksSize = epic.getSubtasks().size();
        int epicToUpdateId = epic.getId();
        Epic updatedEpic = new Epic("New name", "New descr", null, 0);
        inMemoryTaskManager.updateEpic(updatedEpic, epicToUpdateId);
        Epic foundEpic = inMemoryTaskManager.findEpicById(epicToUpdateId);
        int updatedSubtasksSize = foundEpic.getSubtasks().size();

        assertEquals(updatedEpic, foundEpic, "Эпик не обновился.");
        assertEquals(subtasksSize, updatedSubtasksSize, "При обновлении эпика подзадачи неверно подтягиваютсяю");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenUpdatingEmptyEpicList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int epicToUpdateId = 1;
                    Epic updatedEpic = new Epic("New name", "New descr", null, 0);
                    inMemoryTaskManager.updateEpic(updatedEpic, epicToUpdateId);
                });
        assertEquals("Список эпиков пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenUpdatingEpicWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createEpic(epic);
                    int epicToUpdateId = 5;
                    Epic updatedEpic = new Epic("New name", "New descr", null, 0);
                    inMemoryTaskManager.updateEpic(updatedEpic, epicToUpdateId);
                });
        assertEquals("Эпик для обновления не найден по данному номеру.", exception.getMessage());
    }

    @Test
    void shouldUpdateSubtask() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        int subtaskToUpdateId = subtask.getId();
        Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                LocalDateTime.of(2022,7,15,10,20), 30,1);
        inMemoryTaskManager.updateSubtask(updatedSubtask, subtaskToUpdateId);
        Subtask foundSubtask = inMemoryTaskManager.findSubtaskById(subtaskToUpdateId);

        assertEquals(updatedSubtask, foundSubtask, "Подзадача не обновилась.");
        Status status = epic.getStatus();
        assertEquals(Status.IN_PROGRESS, status, "Статус эпика установлен неверно при обновлении подзадачи.");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenUpdatingEmptySubtaskList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int subtaskToUpdateId = 1;
                    Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022,7,20,10,20), 30,1);
                    inMemoryTaskManager.updateSubtask(updatedSubtask, subtaskToUpdateId);
                });
        assertEquals("Список подзадач пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenUpdatingSubtaskWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createEpic(epic);
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,20,10,20), 30,1);
                    inMemoryTaskManager.createSubtask(subtask);
                    int subtaskToUpdateId = 5;
                    Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022,8,25,12,30), 40,1);
                    inMemoryTaskManager.updateSubtask(updatedSubtask, subtaskToUpdateId);
                });
        assertEquals("Подзадача для обновления не найдена по данному номеру.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenUpdatingSubtaskWithWrongEpicId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createEpic(epic);
                    inMemoryTaskManager.createEpic(epic2);
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,20,10,20), 30,1);
                    inMemoryTaskManager.createSubtask(subtask);
                    int subtaskToUpdateId = 3;
                    Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022,8,25,12,30), 40, 5);
                    inMemoryTaskManager.updateSubtask(updatedSubtask, subtaskToUpdateId);
                });
        assertEquals("Не найден эпик, содержащий подзадачу для обновления.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenUpdatingSubtaskOfAnotherEpic() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createEpic(epic);
                    inMemoryTaskManager.createEpic(epic2);
                    Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,20,10,20), 30,1);
                    inMemoryTaskManager.createSubtask(subtask);
                    int subtaskToUpdateId = 3;
                    Subtask updatedSubtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022,8,25,12,30), 40,2);
                    inMemoryTaskManager.updateSubtask(updatedSubtask, subtaskToUpdateId);
                });
        assertEquals("Данная подзадача не входит в указанный эпик.", exception.getMessage());
    }

    @Test
    void shouldDeleteTaskById() {
        inMemoryTaskManager.createTask(task);
        inMemoryTaskManager.findTaskById(task.getId());
        inMemoryTaskManager.deleteTaskById(task.getId());
        Map<Integer, Task> taskList = inMemoryTaskManager.printTaskList();
        List<Task> historyList = inMemoryTaskManager.getHistoryManager().getHistory();
        assertTrue(taskList.isEmpty(), "Задача по id не удалена.");
        assertTrue(historyList.isEmpty(), "Задача не удалена из истории при удалении по id.");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenDeleteTaskInEmptyTaskList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int taskToDeleteId = 1;
                    inMemoryTaskManager.deleteTaskById(taskToDeleteId);
                });
        assertEquals("Список задач пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenDeleteTaskWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createTask(task);
                    int taskToDeleteId = 5;
                    inMemoryTaskManager.deleteTaskById(taskToDeleteId);
                });
        assertEquals("Задачи с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldDeleteSubtaskById() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                LocalDateTime.of(2022,8,25,12,30), 40,1);
        inMemoryTaskManager.createSubtask(subtask);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
        inMemoryTaskManager.findSubtaskById(subtask.getId());
        int subtaskToDeleteId = subtask.getId();
        inMemoryTaskManager.deleteSubtaskById(subtaskToDeleteId);
        Status newStatus = epic.getStatus();

        Map<Integer, Subtask> subtaskList = inMemoryTaskManager.printSubtaskList();
        List<Task> historyList = inMemoryTaskManager.getHistoryManager().getHistory();
        assertTrue(subtaskList.isEmpty(), "Подзадача по id не удалена.");
        assertTrue(historyList.isEmpty(), "Подзадача не удалена из истории при удалении по id.");
        assertEquals(Status.NEW, newStatus, "Неверное обновление статуса при удалении подзадачи.");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenDeleteSubtaskFromEmptySubtaskList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int subtaskToDeleteId = 1;
                    inMemoryTaskManager.deleteSubtaskById(subtaskToDeleteId);
                });
        assertEquals("Список подзадач пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenDeleteSubtaskWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createEpic(epic);
                    Subtask subtask = new Subtask("New name", "New descr", Status.IN_PROGRESS,
                            LocalDateTime.of(2022,8,25,12,30), 40,1);
                    inMemoryTaskManager.createSubtask(subtask);
                    int subtaskToDeleteId = 5;
                    inMemoryTaskManager.deleteSubtaskById(subtaskToDeleteId);
                });
        assertEquals("подзадачи с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldDeleteEpicById() {
        inMemoryTaskManager.createEpic(epic);
        inMemoryTaskManager.findEpicById(epic.getId());
        inMemoryTaskManager.deleteEpicById(epic.getId());
        Map<Integer, Epic> epicList = inMemoryTaskManager.printEpicList();
        List<Task> historyList = inMemoryTaskManager.getHistoryManager().getHistory();
        assertTrue(epicList.isEmpty(), "Эпик по id не удален.");
        assertTrue(historyList.isEmpty(), "Эпик не удален из истории при удалении по id.");
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenDeleteEpicFromEmptyEpicList() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    int epicToDeleteId = 1;
                    inMemoryTaskManager.deleteEpicById(epicToDeleteId);
                });
        assertEquals("Список эпиков пустой.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWhenDeleteEpicWithWrongId() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createEpic(epic);
                    int epicToDeleteId = 5;
                    inMemoryTaskManager.deleteEpicById(epicToDeleteId);
                });
        assertEquals("Эпика с таким номером не существует", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWHenTasksWithSameStartTime() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createTask(new Task("Task1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,10,10,0), 30));
                    inMemoryTaskManager.createTask(new Task("Task1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,10,10,0), 30));
                });
        assertEquals("Задача пересекается по времени.", exception.getMessage());
    }

    @Test
    void shouldThrowTaskManagerExceptionWHenTasksWithSameTime() {
        final TaskManagerException exception = assertThrows(TaskManagerException.class,
                () -> {
                    inMemoryTaskManager.createTask(new Task("Task1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,10,10,0), 30));
                    inMemoryTaskManager.createTask(new Task("Task1", "Descr1", Status.NEW,
                            LocalDateTime.of(2022,7,10,10,0), 30));
                });
        assertEquals("Задача пересекается по времени.", exception.getMessage());
    }

    @Test
    void shouldGetPrioritizedTasks() {
        inMemoryTaskManager.createTask(task);
        inMemoryTaskManager.createTask(new Task("Task2", "Descr1", Status.NEW,
                LocalDateTime.of(2022,12,31,23,0), 30));
        inMemoryTaskManager.createTask(new Task("Task3", "Descr1", Status.NEW,
                null, 30));
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 4);
        inMemoryTaskManager.createSubtask(subtask);
        Set<Task> prioritizedTasks = inMemoryTaskManager.getPrioritizedTasks();
        Optional<Task> firstTask = prioritizedTasks.stream().findFirst();

        assertEquals(4, prioritizedTasks.size(), "Неверное количество задач.");
        assertEquals(task, firstTask.get(), "сортировка по приоритетам неверная(первая задача)");
    }

    @Test
    void shouldComputeEpicStartEndTime() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        Subtask subtask2 = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,15,15,00), 150, 1);
        inMemoryTaskManager.createSubtask(subtask2);

        assertEquals(epic.getStartTime(), subtask2.getStartTime(), "Дата старта эпика рассчитана неверно.");
        assertEquals(epic.getEndTime(), subtask.getEndTime(), "Дата завершения эпика рассчитана неверно.");
    }

    @Test
    void shouldComputeEpicDuration() {
        inMemoryTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,20,10,20), 30, 1);
        inMemoryTaskManager.createSubtask(subtask);
        Subtask subtask2 = new Subtask("Subtask1", "Descr1", Status.NEW,
                LocalDateTime.of(2022,7,15,15,00), 150, 1);
        inMemoryTaskManager.createSubtask(subtask2);

        assertEquals(epic.getDuration(), (subtask.getDuration() + subtask2.getDuration()),
                "Продолжительность эпика рассчитана неверно.");
    }
}