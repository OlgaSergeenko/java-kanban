package manager;

import comparators.TaskIdComparator;
import comparators.TaskStartTimeComparator;
import exceptions.TaskManagerException;
import exceptions.TaskTimeValidationException;
import tasks.*;

import java.util.*;
import java.util.function.Predicate;

public class InMemoryTaskManager implements TaskManager {
    protected int id;
    protected HashMap<Integer, Task> tasks;
    protected HashMap<Integer, Subtask> subtasks;
    protected HashMap<Integer, Epic> epics;
    TaskStartTimeComparator taskStartTimeComparator;
    TaskIdComparator taskIdComparator;
    protected Set<Task> prioritizedTasks;
    protected Set<Task> allTasks;
    protected HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.id = 1;
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.taskStartTimeComparator = new TaskStartTimeComparator();
        this.taskIdComparator = new TaskIdComparator();
        this.prioritizedTasks = new TreeSet<>(taskStartTimeComparator);
        this.allTasks = new TreeSet<>(taskIdComparator);
        this.historyManager = Managers.getDefaultHistory();
    }

    private int generateId() {
        return id++;
    }

    @Override
    public void createTask(Task task) {
        validateTaskTime(task);
        if (task.getId() == 0) {
            int id;
            if (!tasks.isEmpty() || !subtasks.isEmpty() || !epics.isEmpty()) {
                id = 0;
                Optional<Task> t = allTasks.stream().findFirst();
                if (t.isPresent()) {
                    id = t.get().getId() + 1;
                }
            } else {
                id = generateId();
            }
            task.setId(id);
        }
        tasks.put(task.getId(), task);
        addToPrioritizedList(task);
        allTasks.add(task);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic.getId() == 0) {
            int id;
            if (!tasks.isEmpty() || !subtasks.isEmpty() || !epics.isEmpty()) {
                id = 0;
                Optional<Task> t = allTasks.stream().findFirst();
                if (t.isPresent()) {
                    id = t.get().getId() + 1;
                }
            } else {
                id = generateId();
            }
            epic.setId(id);
        }
        if (epic.getStatus() == null) {
            epic.setStatus(Status.NEW);
        }
        epics.put(epic.getId(), epic);
        epic.setEndTime(null);
        allTasks.add(epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        validateTaskTime(subtask);
        if (subtask.getId() == 0) {
            int id;
            if (!tasks.isEmpty() || !subtasks.isEmpty() || !epics.isEmpty()) {
                id = 0;
                Optional<Task> t = allTasks.stream().findFirst();
                if (t.isPresent()) {
                    id = t.get().getId() + 1;
                }
            } else {
                id = generateId();
            }
            subtask.setId(id);
        }
        subtasks.put(subtask.getId(), subtask);
        addToPrioritizedList(subtask);
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new TaskManagerException("Для данной подзадачи еще не создан эпик.");
        }
        epic.addSubtask(subtask.getId());
        computeEpicStatus(epic);
        computeEpicStartEndTime(epic);
        computeEpicDuration(epic);
        allTasks.add(subtask);
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
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new TaskManagerException("Эпик не найден по id.");
        }
        List<Integer> subtasks = epic.getSubtasks();
        if (subtasks == null) {
            throw new TaskManagerException("У данного эпика отсутствуют подзадачи.");
        }
        List<Subtask> subtasksInEpic = new ArrayList<>();
        for (Integer subtask : subtasks) {
            subtasksInEpic.add(this.subtasks.get(subtask));
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
            Subtask subtask = this.subtasks.get(sub);
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
    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        for (Task task : this.tasks.values()) {
            tasks.add(task);
        }
        return tasks;
    }

    @Override
    public List<Subtask> getSubtasks() {
        List<Subtask> tasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            tasks.add(subtask);
        }
        return tasks;
    }

    @Override
    public List<Epic> getEpics() {
        List<Epic> tasks = new ArrayList<>();
        for (Epic epic : epics.values()) {
            tasks.add(epic);
        }
        return tasks;
    }

    @Override
    public void deleteAllTasks() {
        for(Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
            removeFromPrioritizedList(tasks.get(taskId));
            allTasks.remove(tasks.get(taskId));
        }
        tasks.clear();

    }

    @Override
    public void deleteAllSubtasks() {
        for(Integer subTaskId : subtasks.keySet()) {
            historyManager.remove(subTaskId);
            removeFromPrioritizedList(subtasks.get(subTaskId));
            allTasks.remove(subtasks.get(subTaskId));
        }
        subtasks.clear();
        for (Integer id : epics.keySet()) {
            Epic epic = epics.get(id);
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
        for(Integer subTaskId : subtasks.keySet()) {
            historyManager.remove(subTaskId);
            allTasks.remove(subtasks.get(subTaskId));
        }
        for(Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);
            allTasks.remove(epics.get(epicId));
        }
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllTaskTypes() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
        }
        historyManager.clear();
        tasks.clear();
        epics.clear();
        subtasks.clear();
        prioritizedTasks.clear();
        allTasks.clear();
    }

    @Override
    public Task findTaskById(int taskId) {
        if (tasks.isEmpty()) {
            throw new TaskManagerException("Список задач пустой.");
        }
        Task task = null;
        if (tasks.containsKey(taskId)) {
            for (Integer idNumber : tasks.keySet()) {
                if (idNumber == taskId) {
                    task = tasks.get(idNumber);
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
        if (subtasks.isEmpty()) {
            throw new TaskManagerException("Список подзадач пустой.");
        }
        Subtask subtask = null;
        if (subtasks.containsKey(subtaskId)) {
            for (Integer idNumber : subtasks.keySet()) {
                if (idNumber == subtaskId) {
                    subtask = subtasks.get(idNumber);
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
        if (epics.isEmpty()) {
            throw new TaskManagerException("Список эпиков пустой.");
        }
        Epic epic = null;
        if (epics.containsKey(epicId)) {
            for (Integer idNumber : epics.keySet()) {
                if (idNumber == epicId) {
                    epic = epics.get(idNumber);
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
        if (tasks.isEmpty()) {
            throw new TaskManagerException("Список задач пустой.");
        }
        if (!tasks.containsKey(idToUpdate)){
            throw new TaskManagerException("Задача для обновления не найдена по данному номеру.");
        }
        removeFromPrioritizedList(tasks.get(idToUpdate));
        tasks.remove(idToUpdate);
        validateTaskTime(updatedTask);
        tasks.put(idToUpdate, updatedTask);
        addToPrioritizedList(updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        int idToUpdate = updatedEpic.getId();
        if (epics.isEmpty()) {
            throw new TaskManagerException("Список эпиков пустой.");
        }
        if (!epics.containsKey(idToUpdate)){
            throw new TaskManagerException("Эпик для обновления не найден по данному номеру.");
        }
        Epic epicToUpdate = epics.get(idToUpdate);
        List<Integer> subtasks = epicToUpdate.getSubtasks();
        epics.remove(idToUpdate);
        epics.put(idToUpdate, updatedEpic);
        updatedEpic.setSubtasks((ArrayList<Integer>) subtasks);
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        int idToUpdate = updatedSubtask.getId();
        if (subtasks.isEmpty()) {
            throw new TaskManagerException("Список подзадач пустой.");
        }
        if (!subtasks.containsKey(idToUpdate)){
            throw new TaskManagerException("Подзадача для обновления не найдена по данному номеру.");
        }
        removeFromPrioritizedList(subtasks.get(idToUpdate));
        subtasks.remove(idToUpdate);
        validateTaskTime(updatedSubtask);
        subtasks.put(idToUpdate, updatedSubtask);
        addToPrioritizedList(updatedSubtask);
        Epic epic = epics.get(updatedSubtask.getEpicId());
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
        if (tasks.isEmpty()) {
            throw new TaskManagerException("Список задач пустой.");
        }
        if (tasks.containsKey(taskId)) {
            removeFromPrioritizedList(tasks.get(taskId));
            historyManager.remove(taskId);
            tasks.remove(taskId);

        } else {
            throw new TaskManagerException("Задачи с таким номером не существует");
        }
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        if (subtasks.isEmpty()) {
            throw new TaskManagerException("Список подзадач пустой.");
        }
        if (subtasks.containsKey(subtaskId)) {
            Subtask subtask = subtasks.get(subtaskId);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic == null) {
                throw new TaskManagerException("Эпик с данной подзадачей не найден.");
            } else {
                List<Integer> subtasks = epic.getSubtasks();
                subtasks.removeIf(idNumber -> idNumber == subtaskId);
                historyManager.remove(subtaskId);
                removeFromPrioritizedList(subtask);
                this.subtasks.remove(subtaskId);
                computeEpicStatus(epic);
            }
        } else {
            throw new TaskManagerException("подзадачи с таким номером не существует");
        }
    }

    @Override
    public void deleteEpicById(int epicId) {
        if (epics.isEmpty()) {
            throw new TaskManagerException("Список эпиков пустой.");
        }
        if (epics.containsKey(epicId)) {
            Epic epic = epics.get(epicId);
            List<Integer> subtasks = epic.getSubtasks();
            for (Integer subtaskId : subtasks) {
                this.subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            subtasks.clear();
            epics.remove(epicId);
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
        prioritizedTasks.add(task);
    }

    private void removeFromPrioritizedList(Task task) {
        if (task == null) {
            return;
        }
        prioritizedTasks.remove(task);
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    private void validateTaskTime(Task task) {
        if (task.getStartTime() == null) {
            return;
        }
        Set<Task> prioritizedTaskList = getPrioritizedTasks();
        Predicate<Task> filter = t -> t.getStartTime() != null && t.getDuration() != 0;
        List<Task> tasksWithStartTime = new ArrayList<>();
        for (Task t : prioritizedTaskList){
            if (filter.test(t)){
                tasksWithStartTime.add(t);
            }
        }
        if (tasksWithStartTime.isEmpty()) {
            return;
        }
        for (Task t : tasksWithStartTime) {
            if (task.getStartTime().isEqual(t.getStartTime())){
                throw new TaskTimeValidationException("Задача пересекается по времени.");
            } else if (task.getStartTime().isBefore(t.getStartTime())
                    && (task.getEndTime().isAfter(t.getStartTime()))) {
                throw new TaskTimeValidationException("Задача пересекается по времени.");
            } else if (task.getStartTime().isAfter(t.getStartTime())
                    && task.getStartTime().isBefore(t.getEndTime())) {
                throw new TaskTimeValidationException("Задача пересекается по времени.");
            } else {
                continue;
            }
        }
    }
}
