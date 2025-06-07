package thesis.model.domain;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private final String courseId;
    private List<Config> configList = new ArrayList<>();

    public Course(String courseId) {
        this.courseId = courseId;
    }

    public void addConfig(Config config) {
        configList.add(config);
    }

    public String getCourseId() {
        return courseId;
    }

    public List<Config> getConfigList() {
        return configList;
    }
}
