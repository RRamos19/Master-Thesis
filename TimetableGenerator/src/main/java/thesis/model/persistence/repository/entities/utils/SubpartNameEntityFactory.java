package thesis.model.persistence.repository.entities.utils;

import org.hibernate.Session;
import thesis.model.persistence.repository.entities.SubpartNameEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubpartNameEntityFactory {
    private final Session session;
    private final Map<String, SubpartNameEntity> subpartNameEntityMap = new HashMap<>();

    public SubpartNameEntityFactory(Session session) {
        this.session = session;
        updateCache();
    }

    public SubpartNameEntity getOrCreateSubpartName(String subpartId) {
        return subpartNameEntityMap.computeIfAbsent(subpartId, (String id) -> {
            SubpartNameEntity subpartNameEntity = new SubpartNameEntity(id);
            session.persist(subpartNameEntity);
            return subpartNameEntity;
        });
    }

    private void updateCache() {
        List<SubpartNameEntity> storedData = session.createQuery("SELECT a FROM SubpartNameEntity a", SubpartNameEntity.class).getResultList();
        for (SubpartNameEntity subpartNameEntity : storedData) {
            subpartNameEntityMap.put(subpartNameEntity.getName(), subpartNameEntity);
        }
    }
}
