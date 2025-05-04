package thesis.structures;

import java.util.HashMap;
import java.util.Map;

public class Timetable {
    private final String timetableId;
    private final Map<String, AssignedClass> assignedClasses;

    public Timetable(String timetableId){
        this.timetableId = timetableId;
        assignedClasses = new HashMap<>();
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

    public AssignedClass getAssignedClass(String classId) {
        return assignedClasses.get(classId);
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
