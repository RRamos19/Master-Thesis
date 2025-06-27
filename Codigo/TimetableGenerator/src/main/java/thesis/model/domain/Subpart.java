package thesis.model.domain;

import java.util.ArrayList;
import java.util.List;

public class Subpart {
    private final String subpartId;
    private final List<ClassUnit> classUnitList = new ArrayList<>();

    public Subpart(String subpartId) {
        this.subpartId = subpartId;
    }

    public void addClassUnit(ClassUnit classUnit) {
        classUnitList.add(classUnit);
    }

    public String getSubpartId() {
        return subpartId;
    }

    public List<ClassUnit> getClassUnitList() {
        return classUnitList;
    }
}
