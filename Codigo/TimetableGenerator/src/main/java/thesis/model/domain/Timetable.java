package thesis.model.domain;

import java.util.*;

public class Timetable implements Cloneable {
    private String program;
    private List<ScheduledLesson> scheduledLessonList = new ArrayList<>();

    public Timetable() {}

    public Timetable(String program) {
        this.program = program;
    }

    public List<ScheduledLesson> getScheduledLessonList() {
        return scheduledLessonList;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getProgram() {
        return program;
    }

    public void addScheduledLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonList.add(scheduledLesson);
    }

    // TODO: Temporário, eliminar quando deixar de ser necessário
    public void printTimetable() {
        // Organize lessons by [day][slot]
        // The order goes from 0 to 5 from Monday to Saturday
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int numberOfDays = daysOfWeek.length;
        Map<Integer, Map<Integer, ScheduledLesson>> schedule = new HashMap<>();
        for (int i=0; i < numberOfDays; i++) {
            schedule.put(i, new HashMap<>());
        }

        for (ScheduledLesson scheduledLesson : scheduledLessonList) {
            String days = scheduledLesson.getDays();
            int dayslength = days.length();
            if(numberOfDays != dayslength) {
                throw new IllegalArgumentException("Days and numberOfDays are different");
            }

            for(int i=0; i < numberOfDays; i++) {
                if(days.charAt(i) == '1') {
                    schedule.get(i).put(scheduledLesson.getStartSlot(), scheduledLesson);
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
        for (Map<Integer, ScheduledLesson> bySlot : schedule.values()) {
            allSlots.addAll(bySlot.keySet());
        }

        // Print timetable per slot
        for (int slot : allSlots) {
            StringBuilder row = new StringBuilder();
            row.append(String.format("| %-4d", slot));
            for (int i=0; i<numberOfDays; i++) {
                ScheduledLesson lesson = schedule.get(i).get(slot);
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

    @Override
    public Timetable clone() {
        try {
            Timetable clone = (Timetable) super.clone();
            clone.scheduledLessonList = new ArrayList<>(scheduledLessonList);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
