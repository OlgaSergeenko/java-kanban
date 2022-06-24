package manager;

import tasks.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    public static void main(String[] args) {

        FileBackedTasksManager fbtm = Managers.getDefaultFileBacked();
        File file = new File("taskFile.csv");

        Task task1 = new Task(fbtm.generateId(), "Задача1", "Описание1",
                Status.NEW);
        fbtm.createTask(task1);
        Task task2 = new Task (fbtm.generateId(),"Задача2", "Описание2",
                Status.IN_PROGRESS);
        fbtm.createTask(task2);
        Epic epic1 = new Epic(fbtm.generateId(),"Эпик1", "Описание1");
        fbtm.createEpic(epic1);
        Subtask subtask1 = new Subtask(fbtm.generateId(),"Подзадача1 эпик1", "Описание",
                Status.DONE, epic1.getId());
        fbtm.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(fbtm.generateId(),"Подзадача2 эпик1", "Описание",
                Status.NEW, epic1.getId());
        fbtm.createSubtask(subtask2);
        Epic epic2 = new Epic(fbtm.generateId(),"Эпик2", "Описание");
        fbtm.createEpic(epic2);

        fbtm.findTaskById(2);
        fbtm.findSubtaskById(5);
        fbtm.findEpicById(3);

        FileBackedTasksManager fileBackedTasksManager = loadFromFile(file);
        System.out.println(fileBackedTasksManager.historyManager.getHistory());
        System.out.println(fileBackedTasksManager.printTaskList());
        System.out.println(fileBackedTasksManager.printEpicList());
        System.out.println(fileBackedTasksManager.printSubtaskList());
    }

    static FileBackedTasksManager loadFromFile(File file) {;
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager();
        fileBackedTasksManager.readFile(file);
        return fileBackedTasksManager;
    }

    private static String toString(HistoryManager manager) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Task task : manager.getHistory()) {
                sb.append(task.getId()).append(",");
            }
        } catch (NullPointerException exception) {
            System.out.println("История просмотра отсутствует в файле");
        }
        return sb.toString();
    }

    private Task taskFromString(String value) {
        String[] task = value.split(",");
        if (TaskType.valueOf(task[1]) == TaskType.SUBTASK) {
            return new Subtask(Integer.parseInt(task[0]), task[2], task[4], Status.valueOf(task[3]),
                    Integer.parseInt(task[5]));
        } else if (TaskType.valueOf(task[1]) == TaskType.TASK) {
            return new Task(Integer.parseInt(task[0]), task[2], task[4], Status.valueOf(task[3]));
        }
        Epic epic = new Epic(Integer.parseInt(task[0]), task[2], task[4]);
        epic.setStatus(Status.valueOf(task[3]));
        return epic;
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> historyList = new ArrayList<>();
        String[] list = value.split(",");
        for (String s : list) {
            historyList.add(Integer.parseInt(s));
        }
        return historyList;
    }

    private void save() {
        try (FileWriter fileWriter = new FileWriter("taskFile.csv")) {
            fileWriter.write("id,type,name,status,description,epic" + "\n");
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
            System.out.println("Ошибка при попытке записи данных файл");
        }
    }

    private void readFile(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            bufferedReader.readLine();
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                String[] lineSeparated = line.split(",");
                if (!line.isBlank()) {
                    if (TaskType.valueOf(lineSeparated[1]) == TaskType.TASK) {
                        Task task = taskFromString(line);
                        taskList.put(task.getId(), task);
                    } else if (TaskType.valueOf(lineSeparated[1]) == TaskType.SUBTASK) {
                        Task task = taskFromString(line);
                        subtaskList.put(task.getId(), (Subtask) task);
                    } else if (TaskType.valueOf(lineSeparated[1]) == TaskType.EPIC) {
                        Task task = taskFromString(line);
                        epicList.put(task.getId(), (Epic) task);
                    }
                } else {
                        line = bufferedReader.readLine();
                        for (Integer id : historyFromString(line)) {
                            if (taskList.containsKey(id)){
                                Task task = taskList.get(id);
                                historyManager.add(task);
                            } else if (epicList.containsKey(id)){
                                Epic epic = epicList.get(id);
                                historyManager.add(epic);
                            } else {
                                Subtask subtask = subtaskList.get(id);
                                historyManager.add(subtask);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
            ex.printStackTrace();
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
