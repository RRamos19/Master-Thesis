package thesis.model.persistence.repository.entities.utils;

import jakarta.persistence.EntityManager;
import thesis.model.persistence.repository.entities.ConstraintTypeEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstraintTypeFactory {
    private final EntityManager entityManager;
    private final Map<String, ConstraintTypeEntity> constraintTypeEntityMap = new HashMap<>();

    public ConstraintTypeFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
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
        List<ConstraintTypeEntity> storedData = entityManager.createQuery("SELECT a FROM ConstraintTypeEntity a", ConstraintTypeEntity.class).getResultList();
        for (ConstraintTypeEntity constraintTypeEntity : storedData) {
            constraintTypeEntityMap.put(constraintTypeEntity.getName(), constraintTypeEntity);
        }
    }
}
