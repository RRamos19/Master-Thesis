package thesis.model.domain.components;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import thesis.model.exceptions.CheckedIllegalArgumentException;

import java.util.*;

public class Room {
    private final String roomId;
    private final int intId;

    private final List<Time> roomUnavailabilities = new ArrayList<>();
    private final Map<String, Integer> roomDistances = new HashMap<>(); // roomId : distance (Only used for export)
    private final Int2IntMap fastRoomDistances = new Int2IntOpenHashMap();

    public Room(String roomId, int intId) {
        this.roomId = roomId;
        this.intId = intId;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getIntId() {
        return intId;
    }

    public void addUnavailability(String days, String weeks, String startSlot, String length) throws CheckedIllegalArgumentException {
        roomUnavailabilities.add(TimeFactory.create(days, weeks, startSlot, length));
    }

    public void addUnavailability(short days, int weeks, short startSlot, short length) throws CheckedIllegalArgumentException {
        roomUnavailabilities.add(TimeFactory.create(days, weeks, startSlot, length));
    }

    public void addUnavailability(Time time) {
        roomUnavailabilities.add(time);
    }

    public void addRoomDistance(String roomId, int distance) {
        roomDistances.put(roomId, distance);
    }

    public void removeRoomDistance(String roomId) {
        roomDistances.remove(roomId);
    }

    public void fixRoomDistances() {
        for(Map.Entry<String, Integer> roomDistance : roomDistances.entrySet()) {
            fastRoomDistances.put(RoomFastIdFactory.getId(roomDistance.getKey()), roomDistance.getValue().intValue());
        }
    }

    public List<Time> getRoomUnavailabilities() {
        return Collections.unmodifiableList(roomUnavailabilities);
    }

    public Map<String, Integer> getRoomDistances() {
        return Collections.unmodifiableMap(roomDistances);
    }

    public int getRoomDistance(int room2Id) {
        return fastRoomDistances.getOrDefault(room2Id, 0);
    }
}
