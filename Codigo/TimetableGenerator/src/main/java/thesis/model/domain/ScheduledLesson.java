package thesis.model.domain;

import thesis.model.domain.exceptions.CheckedIllegalArgumentException;
import thesis.utils.BitToolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ScheduledLesson {
    private final List<Integer> teacherIds = new ArrayList<>();
    private Time scheduledTime;
    private final String roomId;
    private final String classId;
    private final int nDays;
    private final int nWeeks;
    private final short timeWeight;
    private final short roomWeight;

    private final DataRepository model;

    public ScheduledLesson(DataRepository model, String classId, String roomId, Time time) {
        this.model = model;
        this.classId = classId;
        this.roomId = roomId;
        this.scheduledTime = time;

        TimetableConfiguration timetableConfiguration = model.getTimetableConfiguration();
        this.nDays = timetableConfiguration.getNumDays();
        this.nWeeks = timetableConfiguration.getNumWeeks();
        this.roomWeight = timetableConfiguration.getRoomWeight();
        this.timeWeight = timetableConfiguration.getTimeWeight();
    }

    public ScheduledLesson(DataRepository model, String classId, String roomId, String days, String weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        this(model, classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }

    public ScheduledLesson(DataRepository model, String classId, String roomId, byte days, short weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        this(model, classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }

    public boolean isAvailable() {
        List<Teacher> teachers = getTeachers();
        Room room = getRoom();

        if(room != null) {
            for(Time unavailability : room.getRoomUnavailabilities()) {
                if(scheduledTime.overlaps(unavailability)) {
                    return false;
                }
            }
        }

        if(teachers != null) {
            for(Teacher teacher : teachers) {
                for(Time unavailability : teacher.getTeacherUnavailabilities()) {
                    if(scheduledTime.overlaps(unavailability)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }


    /**
     * Calculates the penalty sum of the Time penalties and Room penalties of the class
     * @return The sum of penalties of a given class
     */
    public int toInt() {
        ClassUnit cls = model.getClassUnit(classId);

        // Should never happen but, for security, an exception is thrown
        if(cls == null) {
            throw new RuntimeException("The scheduled lesson class id " + classId + " isn't present in the model");
        }

        int penalty = 0;

        if(roomId != null) {
            // Add the room penalty
            penalty += cls.getRoomPenalty(roomId) * roomWeight;
        }

        // Add the time penalty
        penalty += cls.getTimePenalty(scheduledTime) * timeWeight;

        return penalty;
    }

    public Time getScheduledTime() {
        return scheduledTime;
    }

    public short getDays() {
        return scheduledTime.getDays();
    }

    public String getDaysBinaryString() {
        return BitToolkit.createSpecificSizeBinaryString(nDays, getDays());
    }

    public int getWeeks() {
        return scheduledTime.getWeeks();
    }

    public String getWeeksBinaryString() {
        return BitToolkit.createSpecificSizeBinaryString(nWeeks, getWeeks());
    }

    public int getStartSlot() {
        return scheduledTime.getStartSlot();
    }

    public int getEndSlot() {
        return scheduledTime.getEndSlot();
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

    public Room getRoom() {
        return roomId != null ? model.getRoom(roomId) : null;
    }

    public List<Teacher> getTeachers() {
        ArrayList<Teacher> teachers = new ArrayList<>();

        for(int teacherId : teacherIds) {
            Teacher teacher = model.getTeacher(teacherId);
            if(teacher != null)
                teachers.add(teacher);
        }

        return teachers.isEmpty() ? null : teachers;
    }

    public String getClassId() {
        return classId;
    }

    public ClassUnit getClassUnit() {
        return model.getClassUnit(classId);
    }

    public void addTeacherId(int teacherId) {
        teacherIds.add(teacherId);
    }

    public List<Integer> getTeacherIds() {
        return Collections.unmodifiableList(teacherIds);
    }

    public DataRepository getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "ClassId: " + classId + " " +
                "RoomId: " + roomId + " " +
                scheduledTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScheduledLesson)) return false;
        ScheduledLesson that = (ScheduledLesson) o;
        return Objects.equals(teacherIds, that.teacherIds) &&
                Objects.equals(scheduledTime, that.scheduledTime) &&
                Objects.equals(roomId, that.roomId) &&
                Objects.equals(classId, that.classId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherIds, scheduledTime, roomId, classId, nDays, nWeeks, model);
    }
}
