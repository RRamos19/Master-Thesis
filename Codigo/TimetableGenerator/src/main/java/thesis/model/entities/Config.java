package thesis.model.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private final String configId;
    private final Map<String, Subpart> subparts = new HashMap<>();

    public Config(String configId) {
        this.configId = configId;
    }

    public Subpart getSubpart(String subpartId) {
        return subparts.get(subpartId);
    }

    public List<Subpart> getSubparts() {
        return new ArrayList<>(subparts.values());
    }

    public void addSubpart(Subpart subpart) {
        subparts.put(subpart.getId(), subpart);
    }

    public String getId() {
        return configId;
    }
}
