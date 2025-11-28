package thesis.model.persistence.entities.utils;

import org.hibernate.Session;
import thesis.model.persistence.entities.ClassUnitNameEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassUnitNameEntityFactory {
    private final Session session;
    private final Map<String, ClassUnitNameEntity> classUnitNameEntityMap = new HashMap<>();

    public ClassUnitNameEntityFactory(Session session) {
        this.session = session;
        updateCache();
    }

    public ClassUnitNameEntity getOrCreateClassUnitName(String classId) {
        return classUnitNameEntityMap.computeIfAbsent(classId, (String id) -> {
            ClassUnitNameEntity classUnitNameEntity = new ClassUnitNameEntity(id);
            session.persist(classUnitNameEntity);
            return classUnitNameEntity;
        });
    }

    private void updateCache() {
        List<ClassUnitNameEntity> storedData = session.createQuery("SELECT a FROM ClassUnitNameEntity a", ClassUnitNameEntity.class).getResultList();
        for (ClassUnitNameEntity classUnitNameEntity : storedData) {
            classUnitNameEntityMap.put(classUnitNameEntity.getName(), classUnitNameEntity);
        }
    }
}
