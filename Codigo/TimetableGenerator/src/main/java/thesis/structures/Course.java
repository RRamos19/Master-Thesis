package thesis.structures;

import java.util.HashMap;

public class Course {
    private final String courseId;
    private final HashMap<String, Config> configs = new HashMap<>();

    public Course(String courseId){
        this.courseId = courseId;
    }

    public void addConfig(Config config){
        configs.put(config.getId(), config);
    }

    public String getId(){
        return courseId;
    }
}
