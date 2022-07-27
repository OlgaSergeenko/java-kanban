package comparators;

import tasks.Task;

import java.util.Comparator;

public class TaskIdComparator implements Comparator<Task> {
    @Override
    public int compare(Task task1, Task task2) {
        if (task1.getId() < task2.getId()) {
            return 1;
        } else if (task1.getId() > task2.getId()) {
            return -1;
        } else {
            return 0;
        }
    }
}
