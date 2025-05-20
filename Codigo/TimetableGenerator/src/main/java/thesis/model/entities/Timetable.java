package thesis.model.entities;

import thesis.model.aggregates.ScheduledClass;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class Timetable {
    private final String timetableId;
    private final Timestamp creationDate;
    private String courseId;
    private final Map<String, ScheduledClass> assignedClasses = new HashMap<>();

    //private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Timetable(String timetableId, Timestamp creationDate, String courseId){
        this.timetableId = timetableId;
        this.creationDate = creationDate;
        this.courseId = courseId;
    }

    public Timetable(){
        this(UUID.randomUUID().toString(), Timestamp.valueOf(LocalDateTime.now()), null);
    }

    public Timetable(String courseId){
        this(UUID.randomUUID().toString(), Timestamp.valueOf(LocalDateTime.now()), courseId);
    }

    public Timetable(String timetableId, String courseId){
        this(timetableId, Timestamp.valueOf(LocalDateTime.now()), courseId);
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void storeAssignedClass(String classId, ScheduledClass scheduledClass) {
        assignedClasses.put(classId, scheduledClass);
    }

    public void storeAssignedClass(String classId, String roomId, String days, int start, int duration, String weeks) {
        assignedClasses.put(classId, new ScheduledClass(classId, roomId, days, start, duration, weeks));
    }

    public String getId(){
        return timetableId;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public ScheduledClass getAssignedClass(String classId) {
        return assignedClasses.get(classId);
    }

    public String getCourseId() {
        return courseId;
    }

    public Map<String, ScheduledClass> getAssignedClasses() {
        return assignedClasses;
    }

    public boolean isScheduleComplete() {
        // TODO: por implementar
        return false;
    }

    // TODO: Temporário, eliminar quando deixar de ser necessário
    public void printTimetable() {
        // Organize lessons by [day][slot]
        // The order goes from 0 to 5 from Monday to Saturday
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int numberOfDays = daysOfWeek.length;
        Map<Integer, Map<Integer, ScheduledClass>> schedule = new HashMap<>();
        for (int i=0; i < numberOfDays; i++) {
            schedule.put(i, new HashMap<>());
        }

        for (ScheduledClass scheduledClass : assignedClasses.values()) {
            String days = scheduledClass.getScheduledTime().getDays();
            int dayslength = days.length();
            if(numberOfDays != dayslength) {
                throw new IllegalArgumentException("Days and numberOfDays are different");
            }

            Time assignedClassTime = scheduledClass.getScheduledTime();
            for(int i=0; i < numberOfDays; i++) {
                if(days.charAt(i) == '1') {
                    schedule.get(i).put(assignedClassTime.getStart(), scheduledClass);
                }
            }
        }

        // Header
        StringBuilder header = new StringBuilder("| Start ");
        for (String day : daysOfWeek) {
            header.append(String.format("| %-15s | Room ", day));
        }
        System.out.println(header.append("|"));

        // Separator
        int columns = numberOfDays * 2 + 1;
        System.out.println("|" + "-".repeat(7 * columns) + "|");

        // Determine all slots used
        Set<Integer> allSlots = new TreeSet<>();
        for (Map<Integer, ScheduledClass> bySlot : schedule.values()) {
            allSlots.addAll(bySlot.keySet());
        }

        // Print timetable per slot
        for (int slot : allSlots) {
            StringBuilder row = new StringBuilder();
            row.append(String.format("| %-4d", slot));
            for (int i=0; i<numberOfDays; i++) {
                ScheduledClass lesson = schedule.get(i).get(slot);
                if (lesson != null) {
                    row.append(String.format("| %-15s | %-4s", lesson.getClassId(), lesson.getRoomId()));
                } else {
                    row.append("|                 |      ");
                }
            }
            row.append("|");
            System.out.println(row);
        }
    }
}
