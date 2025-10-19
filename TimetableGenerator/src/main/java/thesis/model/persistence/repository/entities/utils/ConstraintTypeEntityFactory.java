package thesis.model.persistence.repository.entities.utils;

import org.hibernate.Session;
import thesis.model.persistence.repository.entities.ConstraintTypeEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstraintTypeEntityFactory {
    private final Session session;
    private final Map<String, ConstraintTypeEntity> constraintTypeEntityMap = new HashMap<>();

    public ConstraintTypeEntityFactory(Session session) {
        this.session = session;
        updateCache();
    }

    public ConstraintTypeEntity getConstraintType(String constraintName) {
        ConstraintTypeEntity constraintTypeEntity = constraintTypeEntityMap.get(constraintName);

        if(constraintTypeEntity == null) {
            throw new IllegalArgumentException("Constraint " + constraintName + " not supported!");
        }

        return constraintTypeEntity;
    }

    private void updateCache() {
        List<ConstraintTypeEntity> storedData = session.createQuery("SELECT a FROM ConstraintTypeEntity a", ConstraintTypeEntity.class).getResultList();
        for (ConstraintTypeEntity constraintTypeEntity : storedData) {
            constraintTypeEntityMap.put(constraintTypeEntity.getName(), constraintTypeEntity);
        }
    }
}
