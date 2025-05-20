package thesis.model.entities;

import java.util.ArrayList;
import java.util.List;

public class Distribution {
    private final String type;
    private final Boolean required;
    private final List<String> involvedClasses = new ArrayList<>();
    private final int penalty;

    public Distribution(String type, Boolean required, int penalty){
        this.type = type;
        this.required = required;
        this.penalty = penalty;
    }

    public Distribution(String type, Boolean required){
        this(type, required, 0);
    }

    public void addClassId(String classId){
        involvedClasses.add(classId);
    }

    public String getType() {
        return type;
    }

    public Boolean getRequired() {
        return required;
    }

    public List<String> getInvolvedClasses() {
        return involvedClasses;
    }

    public int getPenalty() {
        return penalty;
    }
}
