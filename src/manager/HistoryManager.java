package manager;

import tasks.Task;

import java.util.List;

public interface HistoryManager {

    void addToHistoryList(Task task);

    List<Task> getHistory();
}
