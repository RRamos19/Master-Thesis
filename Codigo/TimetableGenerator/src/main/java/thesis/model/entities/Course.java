package thesis.model.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Course {
    private final String courseId;
    private final Map<String, Config> configs = new HashMap<>();

    public Course(String courseId) {
        this.courseId = courseId;
    }

    public Config getConfig(String configId) {
        return configs.get(configId);
    }

    public List<Config> getConfigs() {
        return new ArrayList<>(configs.values());
    }

    public void addConfig(Config config) {
        configs.put(config.getId(), config);
    }

    public String getId() {
        return courseId;
    }
}
