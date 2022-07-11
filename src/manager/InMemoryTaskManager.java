package manager;

import comparators.TaskStartTimeComparator;
import exceptions.TaskManagerException;
import exceptions.TaskTimeValidationException;
import tasks.*;

import java.util.*;
import java.util.function.Predicate;

public class InMemoryTaskManager implements TaskManager {
    protected int id;
    protected HashMap<Integer, Task> taskList;
    protected HashMap<Integer, Subtask> subtaskList;
    protected HashMap<Integer, Epic> epicList;
    TaskStartTimeComparator taskStartTimeComparator;
    protected Set<Task> prioritizedTaskList;
    protected HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.id = 1;
        this.taskList = new HashMap<>();
        this.subtaskList = new HashMap<>();
        this.epicList = new HashMap<>();
        this.taskStartTimeComparator = new TaskStartTimeComparator();
        this.prioritizedTaskList = new TreeSet<>(taskStartTimeComparator);
        this.historyManager = Managers.getDefaultHistory();
    }

    private int generateId() {
        return id++;
    }

    @Override
    public void createTask(Task task) {
        validateTaskTime(task);
        if (task.getId() == 0) {
            int id = generateId();
            task.setId(id);
        }
        taskList.put(task.getId(), task);
        addToPrioritizedList(task);
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
        addToPrioritizedList(subtask);
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
        List<Integer> subtasks = epic.getSubtasks();
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
    public List<Task> getTaskList() {
        List<Task> tasks = new ArrayList<>();
        for (Task task : taskList.values()) {
            tasks.add(task);
        }
        return tasks;
    }

    @Override
    public List<Subtask> getSubtaskList() {
        List<Subtask> tasks = new ArrayList<>();
        for (Subtask subtask : subtaskList.values()) {
            tasks.add(subtask);
        }
        return tasks;
    }

    @Override
    public List<Epic> getEpicList() {
        List<Epic> tasks = new ArrayList<>();
        for (Epic epic : epicList.values()) {
            tasks.add(epic);
        }
        return tasks;
    }

    @Override
    public void deleteAllTasks() {
        for(Integer taskId : taskList.keySet()) {
            historyManager.remove(taskId);
            removeFromPrioritizedList(taskList.get(taskId));
        }
        taskList.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for(Integer subTaskId : subtaskList.keySet()) {
            historyManager.remove(subTaskId);
            removeFromPrioritizedList(subtaskList.get(subTaskId));
        }
        subtaskList.clear();
        for (Integer id : epicList.keySet()) {
            Epic epic = epicList.get(id);
            if (epic == null) {
                throw new TaskManagerException("Список эпиков и подзадач пуст.");
            }
            List<Integer> arraylist = epic.getSubtasks();
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
        prioritizedTaskList.clear();
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
    public void updateTask(Task updatedTask) {
        int idToUpdate = updatedTask.getId();
        validateTaskTime(updatedTask);
        if (taskList.isEmpty()) {
            throw new TaskManagerException("Список задач пустой.");
        }
        if (!taskList.containsKey(idToUpdate)){
            throw new TaskManagerException("Задача для обновления не найдена по данному номеру.");
        }
        removeFromPrioritizedList(taskList.get(idToUpdate));
        taskList.remove(idToUpdate);
        taskList.put(idToUpdate, updatedTask);
        addToPrioritizedList(updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        int idToUpdate = updatedEpic.getId();
        if (epicList.isEmpty()) {
            throw new TaskManagerException("Список эпиков пустой.");
        }
        if (!epicList.containsKey(idToUpdate)){
            throw new TaskManagerException("Эпик для обновления не найден по данному номеру.");
        }
        Epic epicToUpdate = epicList.get(idToUpdate);
        List<Integer> subtasks = epicToUpdate.getSubtasks();
        epicList.remove(idToUpdate);
        epicList.put(idToUpdate, updatedEpic);
        updatedEpic.setSubtasks((ArrayList<Integer>) subtasks);
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        int idToUpdate = updatedSubtask.getId();
        validateTaskTime(updatedSubtask);
        if (subtaskList.isEmpty()) {
            throw new TaskManagerException("Список подзадач пустой.");
        }
        if (!subtaskList.containsKey(idToUpdate)){
            throw new TaskManagerException("Подзадача для обновления не найдена по данному номеру.");
        }
        removeFromPrioritizedList(subtaskList.get(idToUpdate));
        subtaskList.remove(idToUpdate);
        subtaskList.put(idToUpdate, updatedSubtask);
        addToPrioritizedList(updatedSubtask);
        Epic epic = epicList.get(updatedSubtask.getEpicId());
        if (epic == null) {
            throw new TaskManagerException("Не найден эпик, содержащий подзадачу для обновления.");
        }
        List<Integer> subtasks = epic.getSubtasks();
        if (!subtasks.contains(idToUpdate)){
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
            removeFromPrioritizedList(taskList.get(taskId));
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
                List<Integer> subtasks = epic.getSubtasks();
                subtasks.removeIf(idNumber -> idNumber == subtaskId);
                historyManager.remove(subtaskId);
                removeFromPrioritizedList(subtask);
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
            List<Integer> subtasks = epic.getSubtasks();
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
    public List<Task> getHistory() {
        if (historyManager.getHistory() == null) {
            throw new TaskManagerException("История просмотров отсутствует.");
        }
        return historyManager.getHistory();
    }

    private void addToPrioritizedList(Task task) {
        if (task == null) {
            return;
        }
        prioritizedTaskList.add(task);
    }

    private void removeFromPrioritizedList(Task task) {
        if (task == null) {
            return;
        }
        prioritizedTaskList.remove(task);
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTaskList;
    }

    private void validateTaskTime(Task task) {
        if (task.getStartTime() == null) {
            return;
        }
        Set<Task> prioritizedTaskList = getPrioritizedTasks();
        for (Task t : prioritizedTaskList) {
            if (task.getStartTime().isEqual(t.getStartTime())){
                throw new TaskTimeValidationException("Задача пересекается по времени.");
            } else if (task.getStartTime().isBefore(t.getStartTime())
                    && (task.getEndTime().isAfter(t.getStartTime()))) {
                throw new TaskTimeValidationException("Задача пересекается по времени.");
            } else if (task.getStartTime().isAfter(t.getStartTime())
                    && task.getStartTime().isBefore(t.getEndTime())) {
                throw new TaskTimeValidationException("Задача пересекается по времени.");
            } else {
                return;
            }
        }
    }
}
