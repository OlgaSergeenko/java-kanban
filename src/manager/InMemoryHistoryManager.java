package manager;


import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager{

    protected Map<Integer, Node> historyList;
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

    public void linkLast(Task task) {
        if (task == null) {
            return;
        }
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

    public void removeNode(Node node) {
        if (node == null) {
            return;
        }
        if (node == firstTask && node == lastTask) {
            historyList.clear();
            firstTask = null;
            lastTask = null;
            return;
        } else if (node == firstTask) {
            firstTask = node.nextTask;
            firstTask.previousTask = null;
            historyList.remove(node.taskValue.getId());
            return;
        } else if (node == lastTask) {
            lastTask = node.previousTask;
            node.previousTask.nextTask = null;
            return;
        }
        node.previousTask.nextTask = node.nextTask;
        node.nextTask.previousTask = node.previousTask;
        historyList.remove(node.taskValue.getId());
    }

    public List<Task> getTasks() {
       ArrayList<Task> historyTaskList= new ArrayList<>();
       Node node = firstTask;
       while (node.nextTask != null) {
           historyTaskList.add(node.taskValue);
           node = node.nextTask;
       }
       historyTaskList.add(lastTask.taskValue);
       return historyTaskList;
    }

    @Override
    public void remove(int id) {
        if (historyList.containsKey(id)) {
            Node node = historyList.get(id);
            if (node == null) {
                return;
            }
            removeNode(node);
            historyList.remove(id);
        }
    }

    @Override
    public void clear() {
        historyList.clear();
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(previousTask, node.previousTask) && Objects.equals(taskValue, node.taskValue) && Objects.equals(nextTask, node.nextTask);
        }

        @Override
        public int hashCode() {
            return Objects.hash(previousTask, taskValue, nextTask);
        }
    }
}


