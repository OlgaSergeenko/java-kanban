import manager.FileBackedTasksManager;
import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        TaskManager fbtm = Managers.getDefault();
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

        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager.loadFromFile(file);
        System.out.println(fileBackedTasksManager.getHistoryManager().getHistory());
        System.out.println(fileBackedTasksManager.printTaskList());
        System.out.println(fileBackedTasksManager.printEpicList());
        System.out.println(fileBackedTasksManager.printSubtaskList());
    }
}
