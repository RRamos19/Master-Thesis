package thesis.model.domain.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course implements TableDisplayable {
    private final String courseId;
    private final List<Config> configList = new ArrayList<>();

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
        return Collections.unmodifiableList(configList);
    }

    @Override
    public String getTableName() {
        return "Courses";
    }

    @Override
    public List<String> getColumnNames() {
        return List.of("Id", "Nº of Configs");
    }

    @Override
    public List<Object> getColumnValues() {
        return List.of(courseId, configList.size());
    }
}
