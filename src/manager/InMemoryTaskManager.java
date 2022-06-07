package manager;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    protected int id;
    protected HashMap<Integer, Task> taskList;
    protected HashMap<Integer, Subtask> subtaskList;
    protected HashMap<Integer, Epic> epicList;
    protected HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.id = 0;
        this.taskList = new HashMap<>();
        this.subtaskList = new HashMap<>();
        this.epicList = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public int generateId() {
        return id++;
    }

    @Override
    public void createTask(Task task) {
        task.setId(id);
        taskList.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(id);
        epicList.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        subtask.setId(id);
        subtaskList.put(subtask.getId(), subtask);
        Epic epic = epicList.get(subtask.getEpicId());
        if (epic == null) {
            System.out.println("Для данной подзадачи еще не создан эпик.");
            return;
        }
        epic.addSubtask(subtask.getId());
        computeEpicStatus(epic);
    }

    public void computeEpicStatus(Epic epic) {
        int counterNew = 0;
        int counterDone = 0;
        ArrayList<Integer> subtasks = epic.getSubtasks();
        if (subtasks == null) {
            System.out.println("В данном эпике нет указанной подзадачи.");
            return;
        }
        for (Integer sub : subtasks) {
            Subtask subtask = subtaskList.get(sub);
            if (subtask.getStatus() == Status.NEW) {
                counterNew++;
            } else if (subtask.getStatus() == Status.DONE) {
                counterDone++;
            }
        }
        if (subtasks.size() == 0) {
            epic.setStatus(Status.NEW);
            return;
        }
        if (counterNew == subtasks.size()) {
            epic.setStatus(Status.NEW);
        } else if (counterDone == subtasks.size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public HashMap<Integer, Task> printTaskList() {
        return taskList;
    }

    @Override
    public HashMap<Integer, Subtask> printSubtaskList() {
        return subtaskList;
    }

    @Override
    public HashMap<Integer, Epic> printEpicList() {
        return epicList;
    }

    @Override
    public void deleteAllTasks() {
        for(Integer taskId : taskList.keySet()) {
            historyManager.remove(taskId);
        }
        taskList.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for(Integer subTaskId : subtaskList.keySet()) {
            historyManager.remove(subTaskId);
        }
        subtaskList.clear();
        for (Integer id : epicList.keySet()) {
            Epic epic = epicList.get(id);
            if (epic == null) {
                System.out.println("Список эпиков и подзадач пуст.");
                return;
            }
            ArrayList<Integer> arraylist = epic.getSubtasks();
            arraylist.clear();
            computeEpicStatus(epic);
        }
    }

    @Override
    public void deleteAllEpics() {
        for(Integer subTaskId : subtaskList.keySet()) {
            historyManager.remove(subTaskId);
        }
        for(Integer epicId : epicList.keySet()) {
            historyManager.remove(epicId);
        }
        subtaskList.clear();
        epicList.clear();
    }

    @Override
    public void deleteAllTaskTypes() {
        historyManager.clear();
        taskList.clear();
        epicList.clear();
        subtaskList.clear();
    }

    @Override
    public Task findTaskById(int taskId) {
        Task task = null;
        if (taskList.containsKey(taskId)) {
            for (Integer idNumber : taskList.keySet()) {
                if (idNumber == taskId) {
                    task = taskList.get(idNumber);
                    historyManager.add(task);
                }
            }
        } else {
            System.out.println("Задачи с таким номером не существует");
        }
        return task;
    }

    @Override
    public Subtask findSubtaskById(int subtaskId) {
        Subtask subtask = null;
        if (subtaskList.containsKey(subtaskId)) {
            for (Integer idNumber : subtaskList.keySet()) {
                if (idNumber == subtaskId) {
                    subtask = subtaskList.get(idNumber);
                    historyManager.add(subtask);
                }
            }
        } else {
            System.out.println("Подзадачи с таким номером не существует");
        }
        return subtask;
    }

    @Override
    public Epic findEpicById(int epicId) {
        Epic epic = null;
        if (epicList.containsKey(epicId)) {
            for (Integer idNumber : epicList.keySet()) {
                if (idNumber == epicId) {
                    epic = epicList.get(idNumber);
                    historyManager.add(epic);
                }
            }
        } else {
            System.out.println("Эпика с таким номером не существует");
        }
        return epic;
    }

    @Override
    public void updateTask(Task updatedTask) {
        taskList.remove(updatedTask.getId());
        taskList.put(updatedTask.getId(), updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        Epic epic = epicList.get(updatedEpic.getId());
        if (epic == null) {
            System.out.println("Указанный эпик не найден.");
            return;
        }
        ArrayList<Integer> subtasks = epic.getSubtasks();
        epicList.remove(updatedEpic.getId());
        epicList.put(updatedEpic.getId(), updatedEpic);
        updatedEpic.setSubtasks(subtasks);
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        subtaskList.remove(updatedSubtask.getId());
        subtaskList.put(updatedSubtask.getId(), updatedSubtask);
        Epic epic = epicList.get(updatedSubtask.getEpicId());
        if (epic == null) {
            System.out.println("Данная подзадача не входит в указанный эпик.");
            return;
        }
        computeEpicStatus(epic);
    }

    @Override
    public void deleteTaskById(int taskId) {
        if (taskList.containsKey(taskId)) {
            historyManager.remove(taskId);
            taskList.remove(taskId);
        } else {
            System.out.println("Задачи с таким номером не существует");
        }
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        if (subtaskList.containsKey(subtaskId)) {
            Subtask subtask = subtaskList.get(subtaskId);
            Epic epic = epicList.get(subtask.getEpicId());
            if (epic == null) {
                System.out.println("Эпик с данной подзадачей не найден.");
            } else {
                ArrayList<Integer> subtasks = epic.getSubtasks();
                subtasks.removeIf(idNumber -> idNumber == subtaskId);
                historyManager.remove(subtaskId);
                subtaskList.remove(subtaskId);
                computeEpicStatus(epic);
            }
        } else {
            System.out.println("подзадачи с таким номером не существует");
        }
    }

    @Override
    public void deleteEpicById(int epicId) {
        if (epicList.containsKey(epicId)) {
            Epic epic = epicList.get(epicId);
            ArrayList<Integer> subtasks = epic.getSubtasks();
            for (Integer subtaskId : subtasks) {
                subtaskList.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            subtasks.clear();
            epicList.remove(epicId);
            historyManager.remove(epicId);
        } else {
            System.out.println("Эпика с таким номером не существует");
        }
    }
}
