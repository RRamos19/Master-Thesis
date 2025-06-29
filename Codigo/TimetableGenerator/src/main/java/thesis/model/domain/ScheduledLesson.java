package thesis.model.domain;

import javafx.util.Pair;
import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScheduledLesson {
    private final List<Integer> teacherIds = new ArrayList<>();
    private Time scheduledTime;
    private String roomId;
    private String classId;

    private DomainModel model;

    public ScheduledLesson(DomainModel model, String classId, String roomId, Time time) {
        this.model = model;
        this.classId = classId;
        this.roomId = roomId;
        this.scheduledTime = time;
    }

    public ScheduledLesson(DomainModel model, String classId, String roomId, String days, String weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        this(model, classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }

    public ScheduledLesson(DomainModel model, String classId, String roomId, byte days, short weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        this(model, classId, roomId, TimeFactory.create(days, weeks, startSlot, length));
    }


    /**
     * Calculates the penalty sum of the Time penalties and Room penalties of the class definition
     * @return The sum of penalties of a given class
     */
    public int toInt() {
        ClassUnit cls = model.getClassUnit(classId);
        // Should never happen but for security an exception is thrown instead of assertion
        if(cls == null) {
            throw new RuntimeException("The scheduled lesson class id " + classId + " isn't present in the model");
        }

        int penalty = 0;

        for(Map.Entry<String, Integer> roomPenalties : cls.getClassRoomPenalties().entrySet()) {
            if(!roomId.equals(roomPenalties.getKey())) {
                penalty += roomPenalties.getValue();
            }
        }

        for(Pair<Time, Integer> timePenalties : cls.getClassTimesList()) {
            if(!scheduledTime.equals(timePenalties.getKey())) {
                penalty += timePenalties.getValue();
            }
        }

        return penalty;
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

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Room getRoom() {
        return model.getRoom(roomId);
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
