package thesis.model.domain;

import java.util.ArrayList;
import java.util.List;

public class ScheduledLesson {
    private List<Integer> teacherIds = new ArrayList<>();
    private Time scheduledTime;
    private String roomId;
    private String classId;

    public ScheduledLesson(String classId, String roomId, String days, String weeks, int startSlot, int length) {
        this.classId = classId;
        this.roomId = roomId;
        this.scheduledTime = new Time(days, weeks, startSlot, length);
    }

    public String getDays() {
        return scheduledTime.getDays();
    }

    public String getWeeks() {
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
