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

    public static class Config {
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

        public static class Subpart {
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
    }
}
