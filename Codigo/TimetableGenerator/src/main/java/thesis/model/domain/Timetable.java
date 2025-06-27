package thesis.model.domain;

import java.util.*;

public class Timetable implements Cloneable {
    private String program;
    private Map<String, ScheduledLesson> scheduledLessonMap = new HashMap<>(); // ClassId : ScheduledLesson

    private List<ClassUnit> unscheduledLessons;

    private DomainModel model;

    public Timetable(DomainModel model) {
        this.model = model;
        this.program = model.getProblemName();
    }

    public List<ScheduledLesson> getScheduledLessonList() {
        return new ArrayList<>(scheduledLessonMap.values());
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getProgram() {
        return program;
    }

    public void addScheduledLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonMap.put(scheduledLesson.getClassId(), scheduledLesson);
    }

    public ScheduledLesson getScheduledLesson(String classId) {
        return scheduledLessonMap.get(classId);
    }

    public void setUnscheduledLessons(List<ClassUnit> unscheduledLessons) {
        this.unscheduledLessons = unscheduledLessons;
    }

    public List<ClassUnit> getUnscheduledLessons() {
        return unscheduledLessons;
    }

    @Override
    public Timetable clone() {
        try {
            Timetable clone = (Timetable) super.clone();
            clone.program = program;
            clone.scheduledLessonMap = new HashMap<>(scheduledLessonMap);
            clone.unscheduledLessons = new ArrayList<>(unscheduledLessons);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public DomainModel getModel() {
        return model;
    }

    public int getTotalValue() {
        int total = 0;
        for(ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
            total += scheduledLesson.toInt();
        }
        return total;
    }

    public int getBestValue() {
        Integer bestValue = null;
        for(ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
            int scheduledLessonValue = scheduledLesson.toInt();
            if(bestValue == null || bestValue > scheduledLessonValue) {
                bestValue = scheduledLessonValue;
            }
        }
        return bestValue;
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

        for (ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
            short days = scheduledLesson.getDays();

            for(int i=0; i < numberOfDays; i++) {
                if((days >> i & 1) == 1) {
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
}
