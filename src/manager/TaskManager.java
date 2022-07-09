package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import java.util.Map;
import java.util.Set;

public interface TaskManager {
    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask);

    Map<Integer, Task> printTaskList();

    Map<Integer, Subtask> printSubtaskList();

    Map<Integer, Epic> printEpicList();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    void deleteAllTaskTypes();

    Task findTaskById(int taskId);

    Subtask findSubtaskById(int subtaskId);

    Epic findEpicById(int epicId);

    void updateTask(Task updatedTask, int taskToUpdateId);

    void updateEpic(Epic updatedEpic, int epicToUpdateId);

    void updateSubtask(Subtask updatedSubtask, int subtaskToUpdateId);

    void deleteTaskById(int taskId);

    void deleteSubtaskById(int subtaskId);

    void deleteEpicById(int epicId);

    HistoryManager getHistoryManager();

    Set<Task> getPrioritizedTasks();
}
