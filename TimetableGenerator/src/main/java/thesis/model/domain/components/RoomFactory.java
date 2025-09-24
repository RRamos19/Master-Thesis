package thesis.model.domain.components;

import java.util.HashMap;
import java.util.Map;

/**
 * Has the objective of assigning an int value to a room when it is created. The int value is only used to obtain the distances between rooms.
 * The usage of the primitive is much more efficient that using objects on maps and other structures.
 */
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
