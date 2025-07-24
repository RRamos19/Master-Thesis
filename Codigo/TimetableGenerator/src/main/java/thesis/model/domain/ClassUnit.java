package thesis.model.domain;

import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

import java.util.*;

public class ClassUnit {
    private final String classId;
    private String parentClassId;

    // Model where the class is stored in. It is used for the creation of all the possible
    // combinations of Time blocks, Rooms and Teachers for this class
    private final DomainModel model;

    private final Map<Time, Integer> classTimesList = new HashMap<>(); // List of pairs of time and associated penalty
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

    public String getParentClassId() {
        return parentClassId;
    }

    public void setParentClassId(String parentClassId) {
        this.parentClassId = parentClassId;
    }

    public void addClassTime(String days, String weeks, int startSlot, int length, int penalty) throws CheckedIllegalArgumentException {
        classTimesList.put(TimeFactory.create(days, weeks, startSlot, length), penalty);
    }

    public Set<Time> getTimeSet() {
        return classTimesList.keySet();
    }

    public int getTimePenalty(Time time) {
        return classTimesList.getOrDefault(time, 0);
    }

    public Map<Time, Integer> getClassTimePenalties() {
        return Collections.unmodifiableMap(classTimesList);
    }

    public void addRoom(String roomId, int penalty) {
        classRoomPenalties.put(roomId, penalty);
    }

    public Set<String> getRoomIds() {
        return classRoomPenalties.keySet();
    }

    public int getRoomPenalty(String roomId) {
        return classRoomPenalties.getOrDefault(roomId, 0);
    }

    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<>();

        for(String roomId : classRoomPenalties.keySet()) {
            Room room = model.getRoom(roomId);
            if(room != null) {
                rooms.add(room);
            }
        }

        return rooms;
    }

    public Map<String, Integer> getClassRoomPenalties() {
        return classRoomPenalties;
    }

    public void addTeacher(int teacherId) {
        classTeacherList.add(teacherId);
    }

    public List<Integer> getTeacherIdList() {
        return Collections.unmodifiableList(classTeacherList);
    }

    public List<Teacher> getTeacherList() {
        List<Teacher> teachers = new ArrayList<>();

        for(int teacherId : classTeacherList) {
            Teacher teacher = model.getTeacher(teacherId);
            if(teacher != null) {
                teachers.add(teacher);
            }
        }

        return teachers;
    }

    public void addConstraint(Constraint r) {
        constraintList.add(r);
    }

    public List<Constraint> getConstraintList() {
        return Collections.unmodifiableList(constraintList);
    }

    public DomainModel getModel() {
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassUnit)) return false;
        ClassUnit classUnit = (ClassUnit) o;
        return Objects.equals(classId, classUnit.classId) &&
                Objects.equals(parentClassId, classUnit.parentClassId) &&
                Objects.equals(classTimesList, classUnit.classTimesList) &&
                Objects.equals(classTeacherList, classUnit.classTeacherList) &&
                Objects.equals(classRoomPenalties, classUnit.classRoomPenalties) &&
                Objects.equals(constraintList, classUnit.constraintList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, parentClassId, model, classTimesList, classTeacherList, classRoomPenalties, constraintList);
    }
}
