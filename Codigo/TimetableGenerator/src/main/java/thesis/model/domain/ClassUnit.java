package thesis.model.domain;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

import java.util.*;

public class ClassUnit {
    private String classId;
    private String parentClassId;

    // Model where the class is stored in. It is used for the creation of all the possible
    // combinations of Time blocks, Rooms and Teachers for this class
    private DomainModel model;

    private final List<Pair<Time, Integer>> classTimesList = new ArrayList<>(); // List of pairs of time and associated penalty
    private final List<Integer> classTeacherList = new ArrayList<>();
    private final Map<String, Integer> classRoomPenalties = new HashMap<>(); // RoomId : penalty
    private final List<Constraint> constraintList = new ArrayList<>();

    public ClassUnit(DomainModel model, String classId) {
        this.model = model;
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

    public void addClassTime(String days, String weeks, int startSlot, int length, int penalty) throws CheckedIllegalArgumentException {
        classTimesList.add(new ImmutablePair<>(TimeFactory.create(days, weeks, startSlot, length), penalty));
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

    public void addConstraint(Constraint r) {
        constraintList.add(r);
    }

    public List<Constraint> getConstraintList() {
        return constraintList;
    }

    public DomainModel getModel() {
        return model;
    }
}
