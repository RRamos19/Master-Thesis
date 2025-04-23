package thesis.structures;

import java.util.HashMap;
import java.util.Map;

public class Course {
    private final String courseId;
    private final Map<String, Config> configs = new HashMap<>();

    public Course(String courseId) {
        this.courseId = courseId;
    }

    public Map<String, Config> getConfigs() {
        return configs;
    }

    public void addConfig(Config config) {
        configs.put(config.getId(), config);
    }

    public String getId() {
        return courseId;
    }
}
