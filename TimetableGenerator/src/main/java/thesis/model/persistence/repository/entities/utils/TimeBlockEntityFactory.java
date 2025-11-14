package thesis.model.persistence.repository.entities.utils;

import org.hibernate.Session;
import thesis.model.persistence.repository.entities.TimeBlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeBlockEntityFactory {
    private final Session session;
    private final Map<List<Object>, TimeBlockEntity> timeBlockEntityMap = new HashMap<>();

    public TimeBlockEntityFactory(Session session) {
        this.session = session;
        updateCache();
    }

    public TimeBlockEntity getOrCreate(short startSlot, short duration, short days, int weeks) {
        return timeBlockEntityMap.computeIfAbsent(List.of(startSlot, duration, days, weeks),
            (k) -> {
                TimeBlockEntity timeBlockEntity = new TimeBlockEntity(startSlot, duration, days, weeks);
                session.persist(timeBlockEntity);
                return timeBlockEntity;
            });
    }

    private void updateCache() {
        List<TimeBlockEntity> storedData = session.createQuery("SELECT a FROM TimeBlockEntity a", TimeBlockEntity.class).getResultList();
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
