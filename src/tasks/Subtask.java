package tasks;

import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name,
                   String description,
                   Status status,
                   LocalDateTime startTime,
                   int duration,
                   int epicId) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
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
                "," + getEndTime() +
                "," + getEpicId() ;
    }
}
