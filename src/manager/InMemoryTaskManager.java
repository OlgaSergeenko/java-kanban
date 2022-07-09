package manager;


import comparators.TaskStartTimeComparator;
import exceptions.TaskManagerException;
import tasks.*;

import java.util.*;
import java.util.function.Predicate;

public class InMemoryTaskManager implements TaskManager {
    protected int id;
    protected HashMap<Integer, Task> taskList;
    protected HashMap<Integer, Subtask> subtaskList;
    protected HashMap<Integer, Epic> epicList;
    protected HistoryManager historyManager;
    protected TaskStartTimeComparator taskStartTimeComparator;

    public InMemoryTaskManager() {
        this.id = 1;
        this.taskList = new HashMap<>();
        this.subtaskList = new HashMap<>();
        this.epicList = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.taskStartTimeComparator = new TaskStartTimeComparator();
    }

    private int generateId() {
        return id++;
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public void createTask(Task task) {
        validateTaskTime(task);
        if (task.getId() == 0) {
            int id = generateId();
            task.setId(id);
        }
        taskList.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic.getId() == 0) {
            int id = generateId();
            epic.setId(id);
        }
        epicList.put(epic.getId(), epic);
        epic.setEndTime(null);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        validateTaskTime(subtask);
        if (subtask.getId() == 0) {
            int id = generateId();
            subtask.setId(id);
        }
        subtaskList.put(subtask.getId(), subtask);
        int epicId = subtask.getEpicId();
        Epic epic = epicList.get(epicId);
        if (epic == null) {
            throw new TaskManagerException("Для данной подзадачи еще не создан эпик.");
        }
        epic.addSubtask(subtask.getId());
        computeEpicStatus(epic);
        computeEpicStartEndTime(epic);
        computeEpicDuration(epic);
    }

    private void computeEpicStartEndTime(Epic epic) {
        Predicate<Subtask> filter = t -> t.getStartTime() != null;
        List<Subtask> subtasksInEpic = getAllSubtasksByEpic(epic.getId());
        if (subtasksInEpic.isEmpty()) {
            return;
        }
        subtasksInEpic.sort(taskStartTimeComparator);
        List<Subtask> sortedSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasksInEpic){
            if (filter.test(subtask)){
                sortedSubtasks.add(subtask);
            }
        }
        if (sortedSubtasks.isEmpty()) {
            return;
        }
        Subtask firstSubtask = sortedSubtasks.get(0);
        Subtask lastSubtask = sortedSubtasks.get(sortedSubtasks.size() - 1);
        epic.setStartTime(firstSubtask.getStartTime());
        epic.setEndTime(lastSubtask.getEndTime());
    }

    private void computeEpicDuration(Epic epic) {
        int epicDuration = 0;
        List<Subtask> subtasksInEpic = getAllSubtasksByEpic(epic.getId());
        if (subtasksInEpic == null) {
            throw new TaskManagerException("У эпика отсутствуют подзадачи. Установить длительность эпика невозможно.");
        }
        for (Subtask subtask : subtasksInEpic) {
            epicDuration = epicDuration + subtask.getDuration();
        }
        epic.setDuration(epicDuration);
    }

    public List<Subtask> getAllSubtasksByEpic(int epicId) {
        Epic epic = epicList.get(epicId);
        if (epic == null) {
            throw new TaskManagerException("Эпик не найден по id.");
        }
        List<Integer> subtasks = epic.getSubtasks();
        if (subtasks == null) {
            throw new TaskManagerException("У данного эпика отсутствуют подзадачи.");
        }
        List<Subtask> subtasksInEpic = new ArrayList<>();
        for (Integer subtask : subtasks) {
            subtasksInEpic.add(subtaskList.get(subtask));
        }
        return subtasksInEpic;
    }

    private void computeEpicStatus(Epic epic) {
        int counterNew = 0;
        int counterDone = 0;
        ArrayList<Integer> subtasks = epic.getSubtasks();
        if (subtasks == null) {
            throw new TaskManagerException("В данном эпике нет указанной подзадачи.");
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
                throw new TaskManagerException("Список эпиков и подзадач пуст.");
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
        for (Epic epic : epicList.values()) {
            epic.getSubtasks().clear();
        }
        historyManager.clear();
        taskList.clear();
        epicList.clear();
        subtaskList.clear();
    }

    @Override
    public Task findTaskById(int taskId) {
        if (taskList.isEmpty()) {
            throw new TaskManagerException("Список задач пустой.");
        }
        Task task = null;
        if (taskList.containsKey(taskId)) {
            for (Integer idNumber : taskList.keySet()) {
                if (idNumber == taskId) {
                    task = taskList.get(idNumber);
                    historyManager.add(task);
                }
            }
        } else {
            throw new TaskManagerException("Задачи с таким номером не существует");
        }
        return task;
    }

    @Override
    public Subtask findSubtaskById(int subtaskId) {
        if (subtaskList.isEmpty()) {
            throw new TaskManagerException("Список подзадач пустой.");
        }
        Subtask subtask = null;
        if (subtaskList.containsKey(subtaskId)) {
            for (Integer idNumber : subtaskList.keySet()) {
                if (idNumber == subtaskId) {
                    subtask = subtaskList.get(idNumber);
                    historyManager.add(subtask);
                }
            }
        } else {
            throw new TaskManagerException("Подзадачи с таким номером не существует");
        }
        return subtask;
    }

    @Override
    public Epic findEpicById(int epicId) {
        if (epicList.isEmpty()) {
            throw new TaskManagerException("Список эпиков пустой.");
        }
        Epic epic = null;
        if (epicList.containsKey(epicId)) {
            for (Integer idNumber : epicList.keySet()) {
                if (idNumber == epicId) {
                    epic = epicList.get(idNumber);
                    historyManager.add(epic);
                }
            }
        } else {
            throw new TaskManagerException("Эпика с таким номером не существует");
        }
        return epic;
    }

    @Override
    public void updateTask(Task updatedTask, int taskToUpdateId) {
        validateTaskTime(updatedTask);
        if (taskList.isEmpty()) {
            throw new TaskManagerException("Список задач пустой.");
        }
        if (!taskList.containsKey(taskToUpdateId)){
            throw new TaskManagerException("Задача для обновления не найдена по данному номеру.");
        }
        taskList.remove(taskToUpdateId);
        updatedTask.setId(taskToUpdateId);
        taskList.put(taskToUpdateId, updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic, int epicToUpdateId) {
        if (epicList.isEmpty()) {
            throw new TaskManagerException("Список эпиков пустой.");
        }
        if (!epicList.containsKey(epicToUpdateId)){
            throw new TaskManagerException("Эпик для обновления не найден по данному номеру.");
        }
        Epic epicToUpdate = epicList.get(epicToUpdateId);
        ArrayList<Integer> subtasks = epicToUpdate.getSubtasks();
        epicList.remove(epicToUpdateId);
        epicList.put(epicToUpdateId, updatedEpic);
        updatedEpic.setSubtasks(subtasks);
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask, int subtaskToUpdateId) {
        validateTaskTime(updatedSubtask);
        if (subtaskList.isEmpty()) {
            throw new TaskManagerException("Список подзадач пустой.");
        }
        if (!subtaskList.containsKey(subtaskToUpdateId)){
            throw new TaskManagerException("Подзадача для обновления не найдена по данному номеру.");
        }
        subtaskList.remove(subtaskToUpdateId);
        subtaskList.put(subtaskToUpdateId, updatedSubtask);
        Epic epic = epicList.get(updatedSubtask.getEpicId());
        if (epic == null) {
            throw new TaskManagerException("Не найден эпик, содержащий подзадачу для обновления.");
        }
        List<Integer> subtasks = epic.getSubtasks();
        if (!subtasks.contains(subtaskToUpdateId)){
            throw new TaskManagerException("Данная подзадача не входит в указанный эпик.");
        }
        computeEpicStatus(epic);
    }

    @Override
    public void deleteTaskById(int taskId) {
        if (taskList.isEmpty()) {
            throw new TaskManagerException("Список задач пустой.");
        }
        if (taskList.containsKey(taskId)) {
            historyManager.remove(taskId);
            taskList.remove(taskId);
        } else {
            throw new TaskManagerException("Задачи с таким номером не существует");
        }
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        if (subtaskList.isEmpty()) {
            throw new TaskManagerException("Список подзадач пустой.");
        }
        if (subtaskList.containsKey(subtaskId)) {
            Subtask subtask = subtaskList.get(subtaskId);
            Epic epic = epicList.get(subtask.getEpicId());
            if (epic == null) {
                throw new TaskManagerException("Эпик с данной подзадачей не найден.");
            } else {
                ArrayList<Integer> subtasks = epic.getSubtasks();
                subtasks.removeIf(idNumber -> idNumber == subtaskId);
                historyManager.remove(subtaskId);
                subtaskList.remove(subtaskId);
                computeEpicStatus(epic);
            }
        } else {
            throw new TaskManagerException("подзадачи с таким номером не существует");
        }
    }

    @Override
    public void deleteEpicById(int epicId) {
        if (epicList.isEmpty()) {
            throw new TaskManagerException("Список эпиков пустой.");
        }
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
            throw new TaskManagerException("Эпика с таким номером не существует");
        }
    }
    @Override
    public Set<Task> getPrioritizedTasks() {
        TreeSet<Task> prioritizedTaskList = new TreeSet<>(taskStartTimeComparator);
        prioritizedTaskList.addAll(taskList.values());
        prioritizedTaskList.addAll(subtaskList.values());
        return prioritizedTaskList;
    }

    private void validateTaskTime(Task task) {
        if (task.getStartTime() == null) {
            return;
        }
        Set<Task> prioritizedTaskList = getPrioritizedTasks();
        for (Task t : prioritizedTaskList) {
            if (task.getStartTime().isEqual(t.getStartTime())){
                throw new TaskManagerException("Задача пересекается по времени.");
            } else if (t.getStartTime().isAfter(task.getStartTime())
                    && (task.getEndTime().isBefore(t.getEndTime())
                    && task.getEndTime().isAfter(t.getStartTime()))) {
                throw new TaskManagerException("Задача пересекается по времени.");
            } else if (t.getStartTime().isAfter(task.getStartTime()) && task.getEndTime().isAfter(t.getEndTime())) {
                throw new TaskManagerException("Задача пересекается по времени.");
            } else if (t.getStartTime().isBefore(task.getStartTime()) && task.getEndTime().isBefore(t.getEndTime())) {
                throw new TaskManagerException("Задача пересекается по времени.");
            } else if (t.getStartTime().isBefore(task.getStartTime())
                    && task.getEndTime().isAfter(t.getEndTime())
            && t.getEndTime().isAfter(task.getStartTime())) {
                throw new TaskManagerException("Задача пересекается по времени.");
            } else {
                return;
            }
        }
    }
}
