package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import java.util.HashMap;


public interface TaskManager {
    int generateId();

    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask);

    HashMap<Integer, Task> printTaskList();

    HashMap<Integer, Subtask> printSubtaskList();

    HashMap<Integer, Epic> printEpicList();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    void deleteAllTaskTypes();

    Task findTaskById(int taskId);

    Subtask findSubtaskById(int subtaskId);

    Epic findEpicById(int epicId);

    void updateTask(Task updatedTask);

    void updateEpic(Epic updatedEpic);

    void updateSubtask(Subtask updatedSubtask);

    void deleteTaskById(int taskId);

    void deleteSubtaskById(int subtaskId);

    void deleteEpicById(int epicId);

}
