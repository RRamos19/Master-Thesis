package thesis.model.domain.components;

import java.util.HashMap;
import java.util.Map;

public class RoomFactory {
    private static final Map<String, Integer> stringToId = new HashMap<>();

    private static int getOrCreateId(String key) {
        return stringToId.computeIfAbsent(key, k -> stringToId.size());
    }

    public static Room createRoom(String roomId) {
        return new Room(roomId, getOrCreateId(roomId));
    }

    public static int getId(String roomId) {
        Integer id = stringToId.get(roomId);

        if (id == null) {
            throw new IllegalArgumentException("Room Id not found: " + roomId);
        }

        return id;
    }
}
