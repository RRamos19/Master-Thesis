package thesis.model.entities;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Timetable {
    private final String timetableId;
    private final Timestamp creationDate;
    private final String courseId;
    private final Map<String, AssignedClass> assignedClasses = new HashMap<>();

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Timetable(String courseId){
        this.timetableId = UUID.randomUUID().toString();
        this.creationDate = Timestamp.valueOf(LocalDateTime.now());
        this.courseId = courseId;
    }

    public Timetable(String timetableId, String courseId){
        this.timetableId = timetableId;
        this.creationDate = Timestamp.valueOf(LocalDateTime.now());
        this.courseId = courseId;
    }

    public Timetable(String timetableId, Timestamp creationDate, String courseId){
        this.timetableId = timetableId;
        this.creationDate = creationDate;
        this.courseId = courseId;
    }

    public void storeAssignedClass(String classId, AssignedClass assignedClass) {
        assignedClasses.put(classId, assignedClass);
    }

    public void storeAssignedClass(String classId, String roomId, String days, int start, int duration, String weeks) {
        assignedClasses.put(classId, new AssignedClass(roomId, days, start, duration, weeks));
    }

    public String getId(){
        return timetableId;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public AssignedClass getAssignedClass(String classId) {
        return assignedClasses.get(classId);
    }

    public String getCourseId() {
        return courseId;
    }

    public Map<String, AssignedClass> getAssignedClasses() {
        return assignedClasses;
    }

    public static class AssignedClass {
        private final String roomId;
        private final Time assignedTime;
        private final String assignedClassId;

        public AssignedClass(String roomId, String days, int start, int duration, String weeks) {
            this.roomId = roomId;
            this.assignedTime = new Time(days, start, duration, weeks);
            this.assignedClassId = UUID.randomUUID().toString();
        }

        public String getRoomId() {
            return roomId;
        }

        public String getAssignedClassId() {
            return assignedClassId;
        }

        public Time getAssignedTime() {
            return assignedTime;
        }
    }
}
