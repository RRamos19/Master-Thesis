package thesis.model.domain.components;

import thesis.model.exceptions.CheckedIllegalArgumentException;

import java.util.*;

public class Room implements TableDisplayable {
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

    public void addUnavailability(Time time) {
        roomUnavailabilities.add(time);
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
        return roomDistances.getOrDefault(room2Id, 0);
    }

    @Override
    public String getTableName() {
        return "Rooms";
    }

    @Override
    public List<String> getColumnNames() {
        return List.of("Id", "Nº of Unavailabilities", "Nº of room distances");
    }

    @Override
    public List<Object> getColumnValues() {
        return List.of(roomId, roomUnavailabilities.size(), roomDistances.size());
    }
}
