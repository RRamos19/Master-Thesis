package thesis.model.persistence.repository.entities.utils;

import org.hibernate.Session;
import thesis.model.persistence.repository.entities.RoomEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomEntityFactory {
    private final Session session;
    private final Map<String, RoomEntity> roomEntityMap = new HashMap<>();

    public RoomEntityFactory(Session session) {
        this.session = session;
        updateCache();
    }

    public RoomEntity getOrCreateRoom(String roomId) {
        return roomEntityMap.computeIfAbsent(roomId, (String id) -> {
            RoomEntity roomEntity = new RoomEntity(id);
            session.persist(roomEntity);
            return roomEntity;
        });
    }

    private void updateCache() {
        List<RoomEntity> storedData = session.createQuery("SELECT a FROM RoomEntity a", RoomEntity.class).getResultList();
        for (RoomEntity roomEntity : storedData) {
            roomEntityMap.put(roomEntity.getName(), roomEntity);
        }
    }
}
