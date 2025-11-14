package thesis.model.persistence.repository.entities.utils;

import org.hibernate.Session;
import thesis.model.persistence.repository.entities.CourseNameEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseNameEntityFactory {
    private final Session session;
    private final Map<String, CourseNameEntity> courseNameEntityMap = new HashMap<>();

    public CourseNameEntityFactory(Session session) {
        this.session = session;
        updateCache();
    }

    public CourseNameEntity getOrCreateCourseName(String courseId) {
        return courseNameEntityMap.computeIfAbsent(courseId, (String id) -> {
            CourseNameEntity courseNameEntity = new CourseNameEntity(id);
            session.persist(courseNameEntity);
            return courseNameEntity;
        });
    }

    private void updateCache() {
        List<CourseNameEntity> storedData = session.createQuery("SELECT a FROM CourseNameEntity a", CourseNameEntity.class).getResultList();
        for (CourseNameEntity courseNameEntity : storedData) {
            courseNameEntityMap.put(courseNameEntity.getName(), courseNameEntity);
        }
    }
}
