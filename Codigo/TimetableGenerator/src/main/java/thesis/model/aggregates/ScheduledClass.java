package thesis.model.aggregates;

import thesis.model.dbms.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScheduledClass {
    private final String classId;
    private final String roomId;
    private final Time scheduledTime;
    private final String scheduledClassId;
    private final List<Integer> assignedTeachers = new ArrayList<>();

    public ScheduledClass(String classId, String roomId, Time time) {
        this.classId = classId;
        this.roomId = roomId;
        this.scheduledTime = time;
        this.scheduledClassId = UUID.randomUUID().toString();
    }

    public ScheduledClass(String classId, Time time) {
        this(classId, null, time);
    }

    public ScheduledClass(String classId, String roomId, String days, int start, int duration, String weeks) {
        this(classId, roomId, new Time(days, start, duration, weeks));
    }

    public ScheduledClass(String classId, String days, int start, int duration, String weeks) {
        this(classId, new Time(days, start, duration, weeks));
    }

    public String getRoomId() {
        return roomId;
    }

    public String getClassId() {
        return classId;
    }

    public String getScheduledClassId() {
        return scheduledClassId;
    }

    public void storeAssignedTeacher(int teacherId) {
        assignedTeachers.add(teacherId);
    }

    public List<Integer> getAssignedTeachers() {
        return assignedTeachers;
    }

    public Time getScheduledTime() {
        return scheduledTime;
    }
}
