package thesis.model.entities;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ClassUnit {
    private final String classId;
    private final String subjectId;
    private final String parentClassId;
    private final List<Pair<String, Integer>> rooms = new ArrayList<>(); // (RoomId, Penalty)
    private final List<Pair<Time, Integer>> times = new ArrayList<>(); // (Time, Penalty)

    public ClassUnit(String subjectId, String classId, String parentClassId){
        this.classId = classId;
        this.subjectId = subjectId;
        this.parentClassId = parentClassId;
    }

    public ClassUnit(String subjectId, String classId){
        this(classId, subjectId, null);
    }

    public String getId(){
        return classId;
    }

    public String getSubjectId() {
        return subjectId;
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
