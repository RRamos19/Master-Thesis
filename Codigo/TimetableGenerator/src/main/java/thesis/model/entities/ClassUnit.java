package thesis.model.entities;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ClassUnit {
    private final String classId;
    private final String parentClassId;
    private final List<Pair<String, Integer>> rooms = new ArrayList<>(); // (RoomId, Penalty)
    private final List<Pair<Time, Integer>> times = new ArrayList<>(); // (Time, Penalty)

    public ClassUnit(String classId){
        this.classId = classId;
        this.parentClassId = null;
    }

    public ClassUnit(String classId, String parentClassId){
        this.classId = classId;
        this.parentClassId = parentClassId;
    }

    public String getId(){
        return classId;
    }

    public String getParentClassId() {
        return parentClassId;
    }

    public void addTime(Time time, int penalty){
        times.add(new Pair<>(time, penalty));
    }

    public void addRoom(String roomId, int penalty){
        rooms.add(new Pair<>(roomId, penalty));
    }
}
