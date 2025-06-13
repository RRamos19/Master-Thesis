package thesis.model.domain;

import javafx.util.Pair;
import thesis.model.domain.restrictions.Restriction;

import java.util.*;

public class ClassUnit {
    private String classId;
    private String parentClassId;

    private final List<Pair<Time, Integer>> classTimesList = new ArrayList<>(); // List of pairs of time and associated penalty
    private final List<Integer> classTeacherList = new ArrayList<>();
    private final Map<String, Integer> classRoomPenalties = new HashMap<>(); // RoomId : penalty
    private final List<Restriction> restrictionList = new ArrayList<>();

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
        classRoomPenalties.put(roomId, penalty);
    }

    public Set<String> getRoomIds() {
        return classRoomPenalties.keySet();
    }

    public Map<String, Integer> getClassRoomPenalties() {
        return classRoomPenalties;
    }

    public void addTeacher(int teacherId) {
        classTeacherList.add(teacherId);
    }

    public List<Integer> getClassTeacherList() {
        return classTeacherList;
    }

    public void addRestriction(Restriction r) {
        restrictionList.add(r);
    }

    public List<Restriction> getRestrictionList() {
        return restrictionList;
    }
}
