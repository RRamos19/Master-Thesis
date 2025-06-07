package thesis.model.domain;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassUnit {
    private String classId;
    private String parentClassId;

    private final List<Pair<Time, Integer>> classTimesList = new ArrayList<>(); // List of pairs of time and associated penalty
    private final List<Integer> classTeacherList = new ArrayList<>();
    private final Map<String, Integer> classRoomIds = new HashMap<>(); // RoomId : penalty

    public ClassUnit(String classId) {
        this.classId = classId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getParentClassId() {
        return parentClassId;
    }

    public void setParentClassId(String parentClassId) {
        this.parentClassId = parentClassId;
    }

    public void addClassTime(String days, String weeks, int startSlot, int length, int penalty) {
        classTimesList.add(new Pair<>(new Time(days, weeks, startSlot, length), penalty));
    }

    public List<Pair<Time, Integer>> getClassTimesList() {
        return classTimesList;
    }

    public void addRoom(String roomId, int penalty) {
        classRoomIds.put(roomId, penalty);
    }

    public Map<String, Integer> getRoomIds() {
        return classRoomIds;
    }

    public void addTeacher(int teacherId) {
        classTeacherList.add(teacherId);
    }

    public List<Integer> getClassTeacherList() {
        return classTeacherList;
    }
}
