package thesis.model.domain;

import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;

public class ScheduledLesson {
    private final List<Integer> teacherIds = new ArrayList<>();
    private Time scheduledTime;
    private String roomId;
    private String classId;

    public ScheduledLesson(String classId, String roomId, Time time) {
        this.classId = classId;
        this.roomId = roomId;
        this.scheduledTime = time;
    }

    public ScheduledLesson(String classId, String roomId, String days, String weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        this(classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }

    public ScheduledLesson(String classId, String roomId, byte days, short weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        this(classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }

    public int toInt() {
        return classId.hashCode() + roomId.hashCode() + scheduledTime.toString().hashCode();
    }

    public Time getScheduledTime() {
        return scheduledTime;
    }

    public short getDays() {
        return scheduledTime.getDays();
    }

    public int getWeeks() {
        return scheduledTime.getWeeks();
    }

    public int getStartSlot() {
        return scheduledTime.getStartSlot();
    }

    public int getLength() {
        return scheduledTime.getLength();
    }

    public void setScheduledTime(Time scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public List<Integer> getTeacherIds() {
        return teacherIds;
    }

    public void addTeacher(int teacherId) {
        teacherIds.add(teacherId);
    }
}
