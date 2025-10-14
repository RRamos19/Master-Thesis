package thesis.model.persistence.repository.entities.utils;

import jakarta.persistence.EntityManager;
import thesis.model.persistence.repository.entities.TimeBlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeBlockFactory {
    private final EntityManager entityManager;
    private final Map<List<Object>, TimeBlockEntity> timeBlockEntityMap = new HashMap<>();

    public TimeBlockFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
        updateCache();
    }

    public TimeBlockEntity getOrCreate(short startSlot, short duration, short days, int weeks) {
        return timeBlockEntityMap.computeIfAbsent(List.of(startSlot, duration, days, weeks),
                (k) -> new TimeBlockEntity(startSlot, duration, days, weeks));
    }

    private void updateCache() {
        List<TimeBlockEntity> storedData = entityManager.createQuery("SELECT a FROM TimeBlockEntity a", TimeBlockEntity.class).getResultList();
        for (TimeBlockEntity timeBlockEntity : storedData) {
            List<Object> key = List.of(
                timeBlockEntity.getStartSlot(),
                timeBlockEntity.getDuration(),
                timeBlockEntity.getDays(),
                timeBlockEntity.getWeeks());

            timeBlockEntityMap.put(key, timeBlockEntity);
        }
    }
}
