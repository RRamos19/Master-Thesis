package thesis.model.entities;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Timetable {
    private final String timetableId;
    private final String creationDate;
    private final String courseId;
    private final Map<String, AssignedClass> assignedClasses = new HashMap<>();

    public Timetable(String timetableId, String courseId){
        this.timetableId = timetableId;
        this.creationDate = LocalDate.now().toString();
        this.courseId = courseId;
    }

    public Timetable(String timetableId, String creationDate, String courseId){
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

    public String getCreationDate() {
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

        public AssignedClass(String roomId, String days, int start, int duration, String weeks) {
            this.roomId = roomId;
            this.assignedTime = new Time(days, start, duration, weeks);
        }

        public String getRoomId() {
            return roomId;
        }

        public Time getAssignedTime() {
            return assignedTime;
        }
    }
}
