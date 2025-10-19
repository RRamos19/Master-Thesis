package thesis.model.domain.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Has the objective of assigning an int value to a room when it is created. The int value is only used to obtain the distances between rooms.
 * The usage of the primitive is much more efficient that using objects on maps and other structures.
 */
public class RoomFastIdFactory {
    private static final Logger logger = LoggerFactory.getLogger(RoomFastIdFactory.class);
    private static final Map<String, Integer> stringToId = new ConcurrentHashMap<>();

    private static int getOrCreateId(String key) {
        return stringToId.computeIfAbsent(key, k -> stringToId.size());
    }

    public static Room createRoom(String roomId) {
        return new Room(roomId, getOrCreateId(roomId));
    }

    public static int getId(String roomId) {
        Integer id = stringToId.get(roomId);

        if (id == null) {
            String message = "Room Id not found: " + roomId;
            logger.error(message);
            throw new IllegalArgumentException(message);
        }

        return id;
    }
}
