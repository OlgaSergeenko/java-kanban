import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = Managers.getDefault();

        Task task1 = new Task(inMemoryTaskManager.generateId(), "Полить цветы", "Взять воду и полить все",
                Status.NEW);
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task (inMemoryTaskManager.generateId(),"Сделать проект", "Понять, создать структуру.",
                Status.IN_PROGRESS);
        inMemoryTaskManager.createTask(task2);
        Epic epic1 = new Epic(inMemoryTaskManager.generateId(),"Уборка", "Проведение уборки во всех комнатах");
        inMemoryTaskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask(inMemoryTaskManager.generateId(),"Уборка в спальне", "полы, пыль",
                Status.DONE, epic1.getId());
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(inMemoryTaskManager.generateId(),"Уборка в гостиной", "полы, пыль, игрушки",
                Status.NEW, epic1.getId());
        inMemoryTaskManager.createSubtask(subtask2);
        Epic epic2 = new Epic(inMemoryTaskManager.generateId(),"Собрать вещи", "Собрать вещи для всех");
        inMemoryTaskManager.createEpic(epic2);
        Subtask subtask3 = new Subtask(inMemoryTaskManager.generateId(),"Вещи для детей", "одежда, игрушки",
                Status.DONE, epic2.getId());
        inMemoryTaskManager.createSubtask(subtask3);
        Subtask subtask4 = new Subtask(inMemoryTaskManager.generateId(), "Собрать вещи Саши", "не забыть памперсы",
                Status.DONE, epic2.getId());
        inMemoryTaskManager.createSubtask(subtask4);

        Task updatedTask = new Task(task2.getId(), "Пройти курс", "начать в четверг", Status.NEW);
        inMemoryTaskManager.updateTask(updatedTask);

        Epic updatedEpic = new Epic(epic2.getId(), "Собрать вещи для детей", "не забыть ничего");
        inMemoryTaskManager.updateEpic(updatedEpic);

        Subtask subtask5 = new Subtask(subtask3.getId(), "Собрать вещи Димы", "одежда, игрушки",
                Status.DONE, epic2.getId());
        inMemoryTaskManager.updateSubtask(subtask5);

        Subtask subtask6 = new Subtask(inMemoryTaskManager.generateId(), "Убраться в ванне", "ванна, туалет",
                Status.IN_PROGRESS, epic1.getId());
        inMemoryTaskManager.createSubtask(subtask6);

        Subtask subtask7 = new Subtask(subtask3.getId(), "Собрать вещи Димы", "не забыть книги",
                Status.DONE, epic2.getId());
        inMemoryTaskManager.updateSubtask(subtask7);

        Subtask subtask8 = new Subtask(15, "Выкинуть мусор","Не забыть пакет на балконе",
                Status.IN_PROGRESS, 8);
//        inMemoryTaskManager.updateSubtask(subtask8);
//
//        System.out.println(inMemoryTaskManager.printTaskList());
//        System.out.println(inMemoryTaskManager.printSubtaskList());
//        System.out.println(inMemoryTaskManager.printEpicList());

        System.out.println(inMemoryTaskManager.findTaskById(1));
        System.out.println(inMemoryTaskManager.findSubtaskById(5));
        System.out.println(inMemoryTaskManager.findEpicById(3));

        System.out.println(inMemoryTaskManager.getHistoryManager().getHistory());

        System.out.println(inMemoryTaskManager.findTaskById(1));
        System.out.println(inMemoryTaskManager.findSubtaskById(5));
        System.out.println(inMemoryTaskManager.findEpicById(3));
        System.out.println(inMemoryTaskManager.findTaskById(1));

        System.out.println(inMemoryTaskManager.getHistoryManager().getHistory());

        System.out.println(inMemoryTaskManager.findTaskById(1));
        System.out.println(inMemoryTaskManager.findSubtaskById(5));
        System.out.println(inMemoryTaskManager.findEpicById(3));
        System.out.println(inMemoryTaskManager.findTaskById(1));
        System.out.println(inMemoryTaskManager.getHistoryManager().getHistory());
    }
}
