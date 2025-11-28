package thesis.model.domain.components;

import thesis.model.domain.InMemoryRepository;
import thesis.model.exceptions.CheckedIllegalArgumentException;
import thesis.utils.BitToolkit;

import java.util.*;

public class ScheduledLesson {
    private final Set<Integer> teacherIds = new HashSet<>();
    private final String roomId;
    private final String classId;
    private Time scheduledTime;
    private short nDays;
    private int nWeeks;
    private short timeWeight;
    private short roomWeight;

    private InMemoryRepository model;

    // Cache the lookups
    private List<Teacher> teacherList;
    private Room room;
    private PenaltySum penalty;
    private boolean calculatePenalty = true;

    public ScheduledLesson(String classId, String roomId, Time time) {
        this.classId = classId;
        this.roomId = roomId;
        this.scheduledTime = time;
    }

    public ScheduledLesson(String classId, String roomId, String days, String weeks, String startSlot, String length) throws CheckedIllegalArgumentException {
        this(classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }

    public ScheduledLesson(String classId, String roomId, short days, int weeks, short startSlot, short length) throws CheckedIllegalArgumentException {
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

    /**
     * Checks if the lesson can be scheduled at the defined time block
     * @return True if it can be scheduled, false otherwise
     */
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
    public PenaltySum toInt() {
        if(calculatePenalty) {
            ClassUnit cls = model.getClassUnit(classId);
            int timePenalty = 0;
            int roomPenalty = 0;

            // Should never happen but, for security, an exception is thrown
            if (cls == null) {
                throw new IllegalStateException("The scheduled lesson class id " + classId + " isn't present in the model");
            }

            if (roomId != null) {
                // Add the room penalty
                roomPenalty += cls.getRoomPenalty(roomId) * roomWeight;
            }

            // Add the time penalty
            timePenalty += cls.getTimePenalty(scheduledTime) * timeWeight;

            penalty = new PenaltySum(roomPenalty, timePenalty);

            calculatePenalty = false;
        }

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

    public short getStartSlot() {
        return scheduledTime.getStartSlot();
    }

    public short getEndSlot() {
        return scheduledTime.getEndSlot();
    }

    public short getLength() {
        return scheduledTime.getLength();
    }

    public String getRoomId() {
        return roomId;
    }

    public Room getRoom() {
        if (model == null) {
            throw new IllegalStateException("Repository not bound yet on Scheduled Class of class id " + classId);
        }

        if(roomId != null && room == null) {
            room = model.getRoom(roomId);;
        }

        return room;
    }

    public List<Teacher> getTeachers() {
        if (model == null) {
            throw new IllegalStateException("Repository not bound yet on Scheduled Class of class id " + classId);
        }

        if(teacherList == null) {
            teacherList = new ArrayList<>();

            for (int teacherId : teacherIds) {
                Teacher teacher = model.getTeacher(teacherId);
                if (teacher != null) {
                    teacherList.add(teacher);
                }
            }
        }

        return teacherList;
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

    public Set<Integer> getTeacherIds() {
        return Collections.unmodifiableSet(teacherIds);
    }

    // Only used when reading from a file
    public void fixTime(Time time) {
        try {
            this.scheduledTime = TimeFactory.create(scheduledTime.getDays(), scheduledTime.getWeeks(), scheduledTime.getStartSlot(), time.getLength());
        } catch (CheckedIllegalArgumentException ignored) {
            // This is impossible because the previous scheduledTime was already created with no error
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
        return Objects.hash(teacherIds, scheduledTime, roomId, classId);
    }
}
