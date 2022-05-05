public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();

        Task task1 = new Task(manager.generateId(), "Полить цветы", "Взять воду и полить все",
                Status.NEW);
        manager.createTask(task1);
        Task task2 = new Task (manager.generateId(),"Сделать проект", "Понять, создать структуру.",
                Status.IN_PROGRESS);
        manager.createTask(task2);
        Epic epic1 = new Epic(manager.generateId(),"Уборка", "Проведение уборки во всех комнатах");
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask(manager.generateId(),"Уборка в спальне", "полы, пыль",
                Status.DONE, epic1.getId());
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(manager.generateId(),"Уборка в гостиной", "полы, пыль, игрушки",
                Status.NEW, epic1.getId());
        manager.createSubtask(subtask2);
        Epic epic2 = new Epic(manager.generateId(),"Собрать вещи", "Собрать вещи для всех");
        manager.createEpic(epic2);
        Subtask subtask3 = new Subtask(manager.generateId(),"Вещи для детей", "одежда, игрушки",
                Status.DONE, epic2.getId());
        manager.createSubtask(subtask3);
        Subtask subtask4 = new Subtask(manager.generateId(), "Собрать вещи Саши", "не забыть памперсы",
                Status.DONE, epic2.getId());
        manager.createSubtask(subtask4);

        Task updatedTask = new Task(task2.getId(), "Пройти курс", "начать в четверг", Status.NEW);
        manager.updateTask(updatedTask);

        Epic updatedEpic = new Epic(epic2.getId(), "Собрать вещи для детей", "не забыть ничего");
        manager.updateEpic(updatedEpic);

        Subtask subtask5 = new Subtask(subtask3.getId(), "Собрать вещи Димы", "одежда, игрушки",
                Status.DONE, epic2.getId());
        manager.updateSubtask(subtask5);

        Subtask subtask6 = new Subtask(manager.generateId(), "Убраться в ванне", "ванна, туалет",
                Status.IN_PROGRESS, epic1.getId());
        manager.createSubtask(subtask6);

        Subtask subtask7 = new Subtask(subtask3.getId(), "Собрать вещи Димы", "не забыть книги",
                Status.IN_PROGRESS, epic2.getId());
        manager.updateSubtask(subtask7);

        System.out.println(manager.printTaskList());
        System.out.println(manager.printSubtaskList());
        System.out.println(manager.printEpicList());

        System.out.println(manager.findTaskById(1));
        System.out.println(manager.findSubtaskById(5));
        System.out.println(manager.findEpicById(3));

        manager.deleteEpicById(3);
        manager.deleteSubtaskById(7);
        manager.deleteSubtaskById(8);
        manager.deleteTaskById(1);
    }
}
