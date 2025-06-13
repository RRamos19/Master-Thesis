package thesis.model.domain.restrictions;

import thesis.model.domain.Timetable;

import java.util.ArrayList;
import java.util.List;

public abstract class Restriction {
    private final String type;
    private final Integer penalty;
    private final boolean required;
    private final List<String> classUnitIdList = new ArrayList<>();

    public Restriction(String type, Integer penalty, boolean required) {
        this.type = type;
        this.penalty = penalty;
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public boolean getRequired() {
        return required;
    }

    public void addClassUnitId(String classUnitId) {
        classUnitIdList.add(classUnitId);
    }

    public List<String> getClassUnitIdList() {
        return classUnitIdList;
    }

    public abstract int evaluate(Timetable solution);
}
