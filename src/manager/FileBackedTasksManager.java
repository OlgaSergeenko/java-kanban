package manager;

import exceptions.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager();
        fileBackedTasksManager.readFile(file);
        return fileBackedTasksManager;
    }

    private static String toString(HistoryManager manager) {
        StringBuilder sb = new StringBuilder();
        for (Task task : manager.getHistory()) {
            sb.append(task.getId()).append(",");
        }
        return sb.toString();
    }

    private Task taskFromString(String value) {
        String[] taskLine = value.split(",");
        Task task = null;
        int id;
        switch (TaskType.valueOf(taskLine[1])) {
            case TASK:
                id = Integer.parseInt(taskLine[0]);
                if (taskLine[5].equals("null")) {
                    task = new Task(taskLine[2], taskLine[4], Status.valueOf(taskLine[3]), null,
                            Optional.of(Integer.parseInt(taskLine[6])).orElse(0));
                } else {
                    task = new Task(taskLine[2], taskLine[4], Status.valueOf(taskLine[3]), LocalDateTime.parse(taskLine[5]),
                            Integer.parseInt(taskLine[6]));
                }
                task.setId(id);
                break;
            case SUBTASK:
                id = Integer.parseInt(taskLine[0]);
                if (taskLine[5].equals("null")) {
                    task = new Subtask(taskLine[2], taskLine[4], Status.valueOf(taskLine[3]),
                            null, Optional.of(Integer.parseInt(taskLine[6])).orElse(0),
                            Integer.parseInt(taskLine[8]));
                } else {
                    task = new Subtask(taskLine[2], taskLine[4], Status.valueOf(taskLine[3]),
                            LocalDateTime.parse(taskLine[5]), Integer.parseInt(taskLine[6]),
                            Integer.parseInt(taskLine[8]));
                }
                task.setId(id);
                break;
            case EPIC:
                id = Integer.parseInt(taskLine[0]);
                if (taskLine[5].equals("null")) {
                    task = new Epic(taskLine[2], taskLine[4], null, Integer.parseInt(taskLine[6]));
                } else {
                    task = new Epic(taskLine[2], taskLine[4], LocalDateTime.parse(taskLine[5]),
                            Integer.parseInt(taskLine[6]));
                    task.setStartTime(LocalDateTime.parse(taskLine[5]));
                    task.setDuration(Integer.parseInt(taskLine[6]));
                    ((Epic) task).setEndTime(LocalDateTime.parse(taskLine[7]));
                }
                task.setId(id);
                task.setStatus(Status.valueOf(taskLine[3]));

                break;
            default:
                System.out.println("Не удается восстановить задачу из файла.");
        }
        return task;
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> historyList = new ArrayList<>();
        String[] list = value.split(",");
        for (String s : list) {
            historyList.add(Integer.parseInt(s));
        }
        return historyList;
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter("taskFile.csv")) {
            fileWriter.write("id,type,name,status,description,epic,startTime,duration" + "\n");
            for (Task value : taskList.values()) {
                fileWriter.write(value.toString() + "\n");
            }
            for (Epic value : epicList.values()) {
                fileWriter.write(value.toString() + "\n");
            }
            for (Subtask value : subtaskList.values()) {
                fileWriter.write(value.toString() + "\n");
            }
            fileWriter.write("\n");

            if (historyManager.getHistory().size() != 0) {
                fileWriter.write(toString(historyManager));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerSaveException("Ошибка сохранения данных. " + e.getMessage());
        }
    }

    private void readFile(File file) {
        try (FileReader fileReader = new FileReader(file)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            bufferedReader.readLine();
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                String[] lineSeparated = line.split(",");
                if (!line.isBlank()) {
                    Task task;
                    switch (TaskType.valueOf(lineSeparated[1])) {
                        case TASK:
                            task = taskFromString(line);
                            int taskId = task.getId();
                            createTask(task);
                            task.setId(taskId);
                            break;
                        case SUBTASK:
                            task = taskFromString(line);
                            int subtaskId = task.getId();
                            createSubtask((Subtask) task);
                            task.setId(subtaskId);
                            break;
                        case EPIC:
                            task = taskFromString(line);
                            LocalDateTime endTime = task.getEndTime();
                            int epicId = task.getId();
                            createEpic((Epic) task);
                            task.setId(epicId);
                            ((Epic) task).setEndTime(endTime);
                            break;
                        default:
                            System.out.println("Не удается прочитать задачу из файла.");
                    }
                } else if (line.isBlank()) {
                    line = bufferedReader.readLine();
                    if (line == null) {
                        return;
                    } else {
                        for (Integer id : historyFromString(line)) {
                            if (taskList.containsKey(id)) {
                                Task task = taskList.get(id);
                                historyManager.add(task);
                            } else if (epicList.containsKey(id)) {
                                Epic epic = epicList.get(id);
                                historyManager.add(epic);
                            } else {
                                Subtask subtask = subtaskList.get(id);
                                historyManager.add(subtask);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Ошибка чтения данных из файла. " + ex.getMessage());
        }
    }

    @Override
    public void createTask(Task task){
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic){
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask){
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void deleteAllTasks(){
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics(){
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllTaskTypes(){
        super.deleteAllTaskTypes();
        save();
    }

    @Override
    public Task findTaskById(int taskId){
        Task task = super.findTaskById(taskId);
        save();
        return task;
    }
    @Override
    public Subtask findSubtaskById(int subtaskId){
        Subtask subtask = super.findSubtaskById(subtaskId);
        save();
        return subtask;
    }

    @Override
    public Epic findEpicById(int epicId){
        Epic epic = super.findEpicById(epicId);
        save();
        return epic;
    }
    @Override
    public void updateTask(Task updatedTask){
        super.updateTask(updatedTask);
        save();
    }

    @Override
    public void updateEpic(Epic updatedEpic){
        super.updateEpic(updatedEpic);
        save();
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask){
        super.updateSubtask(updatedSubtask);
        save();
    }

    @Override
    public void deleteTaskById(int taskId){
        super.deleteTaskById(taskId);
        save();
    }

    @Override
    public void deleteSubtaskById(int subtaskId){
        super.deleteSubtaskById(subtaskId);
        save();
    }

    @Override
    public void deleteEpicById(int epicId){
        super.deleteEpicById(epicId);
        save();
    }
}
