package manager;

import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager{

    private final Map<Integer, Node> historyList;
    private Node firstTask;
    private Node lastTask;

    public InMemoryHistoryManager() {
        this.historyList = new HashMap<>();
        this.firstTask = null;
        this.lastTask = null;
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (historyList.containsKey(task.getId())) {
            Node node = historyList.get(task.getId());
            removeNode(node);
            linkLast(task);
            return;
        }
        linkLast(task);
    }

    private void linkLast(Task task) {
        if (firstTask == null) {
            Node newNode = new Node(null, task, null);
            firstTask = newNode;
            lastTask = firstTask;
            historyList.put(task.getId(), newNode);
        } else {
            Node lastButOneTask = lastTask;
            Node newNode = new Node(lastButOneTask, task, null);
            lastTask = newNode;
            lastButOneTask.nextTask = newNode;
            historyList.put(task.getId(), newNode);
        }
    }

    private void removeNode(Node node) {
        if (node.nextTask == null && node.previousTask == null) {
            historyList.clear();
            firstTask = null;
            lastTask = null;
            return;
        } else if (node.previousTask == null) {
            firstTask = node.nextTask;
            firstTask.previousTask = null;
            historyList.remove(node.taskValue.getId());
            return;
        } else if (node.nextTask == null) {
            lastTask = node.previousTask;
            node.previousTask.nextTask = null;
            return;
        }
        node.previousTask.nextTask = node.nextTask;
        node.nextTask.previousTask = node.previousTask;
        historyList.remove(node.taskValue.getId());
    }

    private List<Task> getTasks() {
       ArrayList<Task> historyTaskList= new ArrayList<>();
       Node node = firstTask;
       if (firstTask != null) {
           while (node.nextTask != null) {
               historyTaskList.add(node.taskValue);
               node = node.nextTask;
           }
       }
       if (lastTask != null) {
           historyTaskList.add(lastTask.taskValue);
       }
       return historyTaskList;
    }

    @Override
    public void remove(int id) {
        if (historyList.containsKey(id)) {
            Node node = historyList.get(id);
            removeNode(node);
        } else {
            return;
        }
    }

    @Override
    public void clear() {
        getHistory().clear();
        historyList.clear();
        firstTask = null;
        lastTask = null;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private static class Node {
        private Node previousTask;
        private final Task taskValue;
        private Node nextTask;

        public Node(Node previousTask, Task taskValue, Node nextTask) {
            this.previousTask = previousTask;
            this.taskValue = taskValue;
            this.nextTask = nextTask;
        }
    }
}
