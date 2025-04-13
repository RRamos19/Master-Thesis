package thesis.structures;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class Class {
    private final String classId;
    private final HashMap<String, Pair<Room, Integer>> rooms = new HashMap<>(); // RoomId: (Room, Penalty)
    private final ArrayList<Pair<Time, Integer>> times = new ArrayList<>();     // (Time, Penalty)

    public Class(String classId){
        this.classId = classId;
    }

    public String getId(){
        return classId;
    }

    public void addTime(Time time, int penalty){
        times.add(new Pair<>(time, penalty));
    }

    public void addRoom(Room room, int penalty){
        rooms.put(room.getId(), new Pair<>(room, penalty));
    }
}
