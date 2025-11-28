package thesis.model.persistence.entities.utils;

import org.hibernate.Session;
import thesis.model.persistence.entities.TeacherEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherEntityFactory {
    private final Session session;
    private final Map<Integer, TeacherEntity> teacherEntityMap = new HashMap<>();

    public TeacherEntityFactory(Session session) {
        this.session = session;
        updateCache();
    }

    public TeacherEntity create(int teacherId, String teacherName) {
        TeacherEntity teacherEntity = new TeacherEntity(teacherId, teacherName);
        session.persist(teacherEntity);
        return teacherEntityMap.put(teacherId, teacherEntity);
    }

    public TeacherEntity get(int teacherId) {
        return teacherEntityMap.get(teacherId);
    }

    private void updateCache() {
        List<TeacherEntity> storedData = session.createQuery("SELECT a FROM TeacherEntity a", TeacherEntity.class).getResultList();
        for (TeacherEntity teacherEntity : storedData) {
            teacherEntityMap.put(teacherEntity.getId(), teacherEntity);
        }
    }
}
