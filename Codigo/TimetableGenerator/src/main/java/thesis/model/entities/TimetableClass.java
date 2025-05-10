package thesis.model.entities;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class TimetableClass {
    private final String classId;
    private final List<Pair<String, Integer>> rooms = new ArrayList<>(); // (RoomId, Penalty)
    private final List<Pair<Time, Integer>> times = new ArrayList<>(); // (Time, Penalty)

    public TimetableClass(String classId){
        this.classId = classId;
    }

    public String getId(){
        return classId;
    }

    public void addTime(Time time, int penalty){
        times.add(new Pair<>(time, penalty));
    }

    public void addRoom(String roomId, int penalty){
        rooms.add(new Pair<>(roomId, penalty));
    }
}
