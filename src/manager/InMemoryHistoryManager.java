package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{

    protected List<Task> historyList;
    private static final int MAX_TASKS_IN_HISTORY = 10;

    public InMemoryHistoryManager() {
        historyList = new ArrayList<>();
    }

    @Override
    public void addToHistoryList(Task task) {
        if (task == null) {
            return;
        }
        if (historyList.size() < MAX_TASKS_IN_HISTORY) {
            historyList.add(task);
        } else {
            historyList.remove(0);
            historyList.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyList;
    }
}


