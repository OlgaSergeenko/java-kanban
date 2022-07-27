package tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Integer> subtasks;
    private LocalDateTime endTime = null;

    public Epic(String name, String description, LocalDateTime startTime, int duration) {
        super(name, description, Status.NEW, null, 0);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Integer id) {
        if (subtasks == null) {
            subtasks = new ArrayList<>();
        }
        subtasks.add(id);
    }

    public List<Integer> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Integer> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasks, epic.subtasks);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }

    @Override
    public String toString() {
            return getId() +
                    "," + getTaskType() +
                    "," + getName() +
                    "," + getStatus() +
                    "," + getDescription() +
                    "," + getStartTime() +
                    "," + getDuration() +
                    "," + getEndTime();
    }
}
