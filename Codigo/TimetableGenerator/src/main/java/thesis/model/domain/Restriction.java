package thesis.model.domain;

import java.util.ArrayList;
import java.util.List;

public class Restriction {
    private final String name;
    private final Integer penalty;
    private final boolean required;
    private List<String> classUnitIdList = new ArrayList<>();

    public Restriction(String name, Integer penalty, boolean required) {
        this.name = name;
        this.penalty = penalty;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public boolean getRequired() {
        return required;
    }

    public void addClassUnit(String classUnitId) {
        classUnitIdList.add(classUnitId);
    }
}
