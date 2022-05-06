package tasks;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subtasks;
    private int qtyDone;
    private int qtyNew;

    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW);
        subtasks = new ArrayList<>();
        this.qtyDone = 0;
        this.qtyNew = 0;
    }

    public void addSubtask(Integer id) {
        subtasks.add(id);
    }

    public ArrayList<Integer> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Integer> subtasks) {
        this.subtasks = subtasks;
    }

    public int getQtyDone() {
        return qtyDone;
    }

    public int getQtyNew() {
        return qtyNew;
    }

    public void setQtyDone(int qtyDone) {
        this.qtyDone = qtyDone;
    }

    public void setQtyNew(int qtyNew) {
        this.qtyNew = qtyNew;
    }

    @Override
    public String toString() {
        subtasks.toString();
        return "tasks.Epic{" +
                "subtasks=" + subtasks + '\'' +
                ", name='" + getName() + '\'' +
                "status =" + getStatus() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return qtyDone == epic.qtyDone
                && qtyNew == epic.qtyNew
                && Objects.equals(subtasks, epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks, qtyDone, qtyNew);
    }
}
