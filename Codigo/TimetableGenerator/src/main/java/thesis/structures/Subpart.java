package thesis.structures;

import java.util.Map;
import java.util.HashMap;

public class Subpart {
    private final String subpartId;
    private final Map<String, Class> classes = new HashMap<>();

    public Subpart(String subpartId) {
        this.subpartId = subpartId;
    }

    public Map<String, Class> getClasses() {
        return classes;
    }

    public void addClass(Class cls) {
        classes.put(cls.getId(), cls);
    }

    public String getId() {
        return subpartId;
    }
}
