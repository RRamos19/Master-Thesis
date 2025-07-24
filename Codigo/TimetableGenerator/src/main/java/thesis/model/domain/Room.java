package thesis.model.domain;

import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

import java.util.*;

public class Room {
    private final String roomId;

    private final List<Time> roomUnavailabilities = new ArrayList<>();
    private final Map<String, Integer> roomDistances = new HashMap<>(); // roomId : distance

    public Room(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void addUnavailability(String days, String weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        roomUnavailabilities.add(TimeFactory.create(days, weeks, startSlot, length));
    }

    public void addRoomDistance(String roomId, int distance) {
        roomDistances.put(roomId, distance);
    }

    public List<Time> getRoomUnavailabilities() {
        return Collections.unmodifiableList(roomUnavailabilities);
    }

    public Map<String, Integer> getRoomDistances() {
        return Collections.unmodifiableMap(roomDistances);
    }

    public int getRoomDistance(String room2Id) {
        Integer distance = roomDistances.get(room2Id);
        return distance != null ? distance : 0;
    }
}
