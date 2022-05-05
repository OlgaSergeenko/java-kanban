import java.util.ArrayList;

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
        return "Epic{" +
                "subtasks=" + subtasks + '\'' + ", name='" + getName() + '\'' + "status =" + getStatus() +
                '}';
    }
}
