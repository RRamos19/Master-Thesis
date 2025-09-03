package thesis.model.domain.elements;

import thesis.model.domain.InMemoryRepository;
import thesis.model.exceptions.CheckedIllegalArgumentException;
import thesis.utils.BitToolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ScheduledLesson {
    private final List<Integer> teacherIds = new ArrayList<>();
    private final String roomId;
    private final String classId;
    private Time scheduledTime;
    private short nDays;
    private int nWeeks;
    private short timeWeight;
    private short roomWeight;

    private InMemoryRepository model;

    public ScheduledLesson(String classId, String roomId, Time time) {
        this.classId = classId;
        this.roomId = roomId;
        this.scheduledTime = time;
    }

    public ScheduledLesson(String classId, String roomId, String days, String weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        this(classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }

    public ScheduledLesson(String classId, String roomId, short days, int weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        this(classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }

    public void bindModel(InMemoryRepository model) {
        this.model = model;

        TimetableConfiguration timetableConfiguration = model.getTimetableConfiguration();
        this.nDays = timetableConfiguration.getNumDays();
        this.nWeeks = timetableConfiguration.getNumWeeks();
        this.roomWeight = timetableConfiguration.getRoomWeight();
        this.timeWeight = timetableConfiguration.getTimeWeight();
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
            throw new IllegalStateException("The scheduled lesson class id " + classId + " isn't present in the model");
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

    public String getRoomId() {
        return roomId;
    }

    public Room getRoom() {
        if (model == null) {
            throw new IllegalStateException("Repository not bound yet on Scheduled Class of class id " + classId);
        }

        return roomId != null ? model.getRoom(roomId) : null;
    }

    public List<Teacher> getTeachers() {
        if (model == null) {
            throw new IllegalStateException("Repository not bound yet on Scheduled Class of class id " + classId);
        }

        ArrayList<Teacher> teachers = new ArrayList<>();

        for(int teacherId : teacherIds) {
            Teacher teacher = model.getTeacher(teacherId);
            if(teacher != null)
                teachers.add(teacher);
        }

        return teachers;
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

    public void fixTime(Time time) {
        try {
            this.scheduledTime = TimeFactory.create(scheduledTime.getDays(), scheduledTime.getWeeks(), scheduledTime.getStartSlot(), time.getLength());
        } catch (CheckedIllegalArgumentException ignored) {
            // This is impossible because the scheduledTime was already created with no error
        }
    }

    public InMemoryRepository getModel() {
        return model;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("ClassId: " + classId + " ");

        if(roomId != null) {
            result.append("RoomId: ").append(roomId).append(" ");
        }

        if(!teacherIds.isEmpty()) {
            result.append("Teachers: ");

            boolean multipleTeachers = false;
            for(int id : teacherIds) {
                result.append(id);
                if(multipleTeachers) {
                    result.append(",");
                }
                multipleTeachers = true;
            }

            result.append(" ");
        }

        result.append(scheduledTime);

        return result.toString();
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
