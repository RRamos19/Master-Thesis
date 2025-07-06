package thesis.model.domain;

import java.util.*;

public class Timetable implements Cloneable {
    private String program;
    private Map<String, ScheduledLesson> scheduledLessonMap = new HashMap<>(); // ClassId : ScheduledLesson

    public Timetable(String programName) {
        this.program = programName;
    }

    public Timetable() {}

    public List<ScheduledLesson> getScheduledLessonList() {
        return new ArrayList<>(scheduledLessonMap.values());
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public void addScheduledLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonMap.put(scheduledLesson.getClassId(), scheduledLesson);
    }

    public ScheduledLesson getScheduledLesson(String classId) {
        return scheduledLessonMap.get(classId);
    }

    @Override
    public Timetable clone() {
        try {
            Timetable clone = (Timetable) super.clone();
            clone.program = program;
            clone.scheduledLessonMap = new HashMap<>(scheduledLessonMap);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    // TODO: Temporário, eliminar quando deixar de ser necessário
    public void printTimetable() {
        // Organize lessons by [day][slot]
        // The order goes from 0 to 6 from Monday to Sunday
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int numberOfDays = daysOfWeek.length;
        Map<Integer, Map<String, ScheduledLesson>> schedule = new HashMap<>();
        for (int i=0; i < numberOfDays; i++) {
            schedule.put(i, new HashMap<>());
        }

        for (ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
            short days = scheduledLesson.getDays();

            for(int i=0; i < numberOfDays; i++) {
                if((days >> i & 1) == 1) {
                    schedule.get(i).put(scheduledLesson.getStartSlot() + " - " + scheduledLesson.getEndSlot(), scheduledLesson);
                }
            }
        }

        for (int i=0; i<numberOfDays; i++) {
            String day = daysOfWeek[i];
            // Header
            System.out.printf("| %-15s | Time | Room |\n", day);

            // Determine all slots used
            Set<String> allSlots = new TreeSet<>();
            for (Map<String, ScheduledLesson> bySlot : schedule.values()) {
                allSlots.addAll(bySlot.keySet());
            }

            // Print timetable per slot
            for (String slot : allSlots) {
                StringBuilder row = new StringBuilder();
                row.append(String.format("| %-15s ", slot));

                ScheduledLesson lesson = schedule.get(i).get(slot);
                if (lesson != null) {
                    row.append(String.format("| %-5s | %-4s", lesson.getClassId(), lesson.getRoomId()));
                }
                row.append("|");

                System.out.println(row);
            }
        }
    }
}
