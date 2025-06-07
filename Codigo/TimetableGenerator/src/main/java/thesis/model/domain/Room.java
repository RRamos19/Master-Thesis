package thesis.model.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    private String roomId;

    private List<Time> roomUnavailabilities = new ArrayList<>();
    private Map<String, Integer> roomDistances = new HashMap<>(); // roomId : distance

    public Room(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void addUnavailability(String days, String weeks, int startSlot, int length) {
        roomUnavailabilities.add(new Time(days, weeks, startSlot, length));
    }

    public void addRoomDistance(String roomId, int distance) {
        roomDistances.put(roomId, distance);
    }
}
