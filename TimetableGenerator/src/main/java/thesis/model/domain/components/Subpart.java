package thesis.model.domain.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Subpart implements TableDisplayable {
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
        return Collections.unmodifiableList(classUnitList);
    }

    @Override
    public String getTableName() {
        return "Subparts";
    }

    @Override
    public List<String> getColumnNames() {
        return List.of("Id", "Nº of Classes");
    }

    @Override
    public List<Object> getColumnValues() {
        return List.of(subpartId, classUnitList.size());
    }

    @Override
    public boolean isOptimizable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }
}
