import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = Managers.getDefault();

        Task task1 = new Task(inMemoryTaskManager.generateId(), "Задача1", "Описание1",
                Status.NEW);
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task (inMemoryTaskManager.generateId(),"Задача2", "Описание2",
                Status.IN_PROGRESS);
        inMemoryTaskManager.createTask(task2);
        Epic epic1 = new Epic(inMemoryTaskManager.generateId(),"Эпик1", "Описание1");
        inMemoryTaskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask(inMemoryTaskManager.generateId(),"Подзадача1 эпик1", "Описание",
                Status.DONE, epic1.getId());
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(inMemoryTaskManager.generateId(),"Подзадача2 эпик1", "Описание",
                Status.NEW, epic1.getId());
        inMemoryTaskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask(inMemoryTaskManager.generateId(),"Подзадача3 эпик1", "Описание",
                Status.DONE, epic1.getId());
        inMemoryTaskManager.createSubtask(subtask3);
        Epic epic2 = new Epic(inMemoryTaskManager.generateId(),"Эпик2", "Описание");
        inMemoryTaskManager.createEpic(epic2);

//        System.out.println(inMemoryTaskManager.printTaskList());
//        System.out.println(inMemoryTaskManager.printEpicList());
//        System.out.println(inMemoryTaskManager.printSubtaskList());

        System.out.println(inMemoryTaskManager.findTaskById(2));
        System.out.println(inMemoryTaskManager.findSubtaskById(6));
        System.out.println(inMemoryTaskManager.findSubtaskById(4));
        System.out.println(inMemoryTaskManager.findEpicById(7));
        System.out.println(inMemoryTaskManager.findEpicById(7));
        System.out.println(inMemoryTaskManager.findTaskById(2));
        System.out.println(inMemoryTaskManager.findSubtaskById(5));
        System.out.println(inMemoryTaskManager.findEpicById(3));
        System.out.println(inMemoryTaskManager.findTaskById(1));
        System.out.println(inMemoryTaskManager.findEpicById(3));

        inMemoryTaskManager.deleteTaskById(2);
        inMemoryTaskManager.deleteEpicById(3);
        inMemoryTaskManager.deleteSubtaskById(5);

        System.out.println(inMemoryTaskManager.getHistoryManager().getHistory());

    }
}
