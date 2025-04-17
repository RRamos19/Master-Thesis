package thesis.structures;

import javafx.util.Pair;

import java.util.ArrayList;

public class Class {
    private final String classId;
    private final ArrayList<Pair<String, Integer>> rooms = new ArrayList<>(); // (RoomId, Penalty)
    private final ArrayList<Pair<Time, Integer>> times = new ArrayList<>(); // (Time, Penalty)

    public Class(String classId){
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
