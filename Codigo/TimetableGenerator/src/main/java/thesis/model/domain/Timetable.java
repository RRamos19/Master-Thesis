package thesis.model.domain;

import java.util.*;

public class Timetable implements Cloneable {
    private String program;
    private long runtime;                                                      // Sum of the durations of the initial solution and optimization algorithms
    private Map<String, ScheduledLesson> scheduledLessonMap = new HashMap<>(); // ClassId : ScheduledLesson
    Set<Constraint> involvedConstraints = new HashSet<>();                     // Contains the constraints of the classes

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

    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    public long getRuntime() {
        return runtime;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public void addScheduledLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonMap.put(scheduledLesson.getClassId(), scheduledLesson);

        ClassUnit classUnit = scheduledLesson.getClassUnit();
        if(classUnit == null) { // TODO: temporário, falta corrigir o input de dados e o caso das aulas poderem ser null
            throw new RuntimeException("Class Unit of a scheduled class couldn't be found");
        }

        involvedConstraints.addAll(classUnit.getConstraintList());
    }

    public ScheduledLesson getScheduledLesson(String classId) {
        return scheduledLessonMap.get(classId);
    }

    public boolean isTimeFree(Time time) {
        for(ScheduledLesson lesson : scheduledLessonMap.values()) {
            if(lesson.getScheduledTime().overlaps(time)) {
                return false;
            }
        }

        return true;
    }

    public int cost() {
        int cost = 0;

        // Add the Time and Room penalties
        for(ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
            cost += scheduledLesson.toInt();
        }

        // Add the soft constraint penalties (only if they were violated)
        for(Constraint c : involvedConstraints) {
            cost += c.computePenalties(this);
        }

        return cost;
    }

    @Override
    public Timetable clone() {
        try {
            Timetable clone = (Timetable) super.clone();
            clone.program = program;
            clone.scheduledLessonMap = new HashMap<>(scheduledLessonMap);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
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
            Set<String> allSlots = new TreeSet<>(schedule.get(i).keySet());

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
