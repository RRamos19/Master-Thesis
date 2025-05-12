package thesis.model.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Subpart {
    private final String subpartId;
    private final Map<String, ClassUnit> classes = new HashMap<>();

    public Subpart(String subpartId) {
        this.subpartId = subpartId;
    }

    public ClassUnit getClassUnit(String classId) {
        return classes.get(classId);
    }

    public List<ClassUnit> getClasses() {
        return new ArrayList<>(classes.values());
    }

    public void addClass(ClassUnit cls) {
        classes.put(cls.getId(), cls);
    }

    public String getId() {
        return subpartId;
    }
}
