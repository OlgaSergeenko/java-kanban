package tasks;

import java.util.Objects;

public class Task {
    private static final TaskType TASK_TYPE = TaskType.TASK;
    protected int id;
    protected final String name;
    protected final String description;
    protected Status status;



    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskType getTaskType() {
        return TASK_TYPE;
    }

    @Override
    public String toString() {
        return getId() +
                "," + getTaskType() +
                "," + getName() +
                "," + getStatus() +
                "," + getDescription();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id
                && Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status);
    }
}
