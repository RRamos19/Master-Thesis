package thesis.structures;

import java.util.HashMap;
import java.util.Map;

public class Config {
    private final String configId;
    private final Map<String, Subpart> subparts = new HashMap<>();

    public Config(String configId) {
        this.configId = configId;
    }

    public Map<String, Subpart> getSubparts() {
        return subparts;
    }

    public void addSubpart(Subpart subpart) {
        subparts.put(subpart.getId(), subpart);
    }

    public String getId() {
        return configId;
    }
}
