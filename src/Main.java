import manager.FileBackedTasksManager;
import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;

public class Main {

    public static void main(String[] args) {
        TaskManager fbtm = Managers.getDefault();
        File file = new File("taskFile.csv");
        Task task1 = new Task("Задача1", "Описание1", Status.NEW,
                LocalDateTime.of(2022, Month.JULY, 9, 22,15), 60);
        Task task2 = new Task ("Задача2", "Описание2", Status.IN_PROGRESS,
                LocalDateTime.of(2022, Month.JULY, 20, 12,0), 90);
        Epic epic1 = new Epic("Эпик1", "Описание1", null, 0);
        Subtask subtask1 = new Subtask("Подзадача1 эпик1", "Описание", Status.DONE,
                null, 0,3);
        Subtask subtask2 = new Subtask("Подзадача2 эпик1", "Описание", Status.NEW,
                LocalDateTime.of(2022, Month.JULY, 19, 22,15), 25, 3);
        Epic epic2 = new Epic("Эпик2", "Описание", null, 0);

        fbtm.createTask(task1);
        fbtm.createTask(task2);
        fbtm.createEpic(epic1);
        fbtm.createSubtask(subtask1);
        fbtm.createSubtask(subtask2);
        fbtm.createEpic(epic2);
        fbtm.deleteTaskById(2);

        fbtm.findTaskById(1);
        fbtm.findSubtaskById(4);
        fbtm.findEpicById(3);

        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager.loadFromFile(file);
        System.out.println("-----HISTORY------");
        System.out.println(fileBackedTasksManager.getHistory());
        System.out.println("-----LISTS------");
        System.out.println(fileBackedTasksManager.getTaskList());
        System.out.println(fileBackedTasksManager.getEpicList());
        System.out.println(fileBackedTasksManager.getSubtaskList());
        System.out.println("-----PRIORITIES------");
        System.out.println(fileBackedTasksManager.getPrioritizedTasks());
    }
}
