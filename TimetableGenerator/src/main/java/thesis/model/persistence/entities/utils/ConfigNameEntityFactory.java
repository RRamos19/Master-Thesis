package thesis.model.persistence.entities.utils;

import org.hibernate.Session;
import thesis.model.persistence.entities.ConfigNameEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigNameEntityFactory {
    private final Session session;
    private final Map<String, ConfigNameEntity> configNameEntityMap = new HashMap<>();

    public ConfigNameEntityFactory(Session session) {
        this.session = session;
        updateCache();
    }

    public ConfigNameEntity getOrCreateConfigName(String configId) {
        return configNameEntityMap.computeIfAbsent(configId, (String id) -> {
            ConfigNameEntity configNameEntity = new ConfigNameEntity(id);
            session.persist(configNameEntity);
            return configNameEntity;
        });
    }

    private void updateCache() {
        List<ConfigNameEntity> storedData = session.createQuery("SELECT a FROM ConfigNameEntity a", ConfigNameEntity.class).getResultList();
        for (ConfigNameEntity configNameEntity : storedData) {
            configNameEntityMap.put(configNameEntity.getName(), configNameEntity);
        }
    }
}
