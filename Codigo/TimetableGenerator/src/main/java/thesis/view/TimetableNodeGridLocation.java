package thesis.view;

import javafx.scene.Node;

public class TimetableNodeGridLocation {
    private final Node node;
    private final int day;
    private final int start;
    private final int duration;
    private final int subColumn;

    public TimetableNodeGridLocation(Node node, int day, int start, int duration, int subColumn) {
        this.node = node;
        this.day = day;
        this.start = start;
        this.duration = duration;
        this.subColumn = subColumn;
    }

    public Node getNode() {
        return node;
    }

    public int getDay() {
        return day;
    }

    public int getStart() {
        return start;
    }

    public int getDuration() {
        return duration;
    }

    public int getSubColumn() {
        return subColumn;
    }
}
