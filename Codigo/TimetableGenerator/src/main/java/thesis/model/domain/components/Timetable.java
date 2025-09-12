package thesis.model.domain.components;

import thesis.model.domain.InMemoryRepository;
import thesis.model.parser.XmlResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Timetable implements Cloneable, TableDisplayable, XmlResult {
    private static final DateTimeFormatter dateOfCreationFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
    private LocalDateTime dateOfCreation = LocalDateTime.now();
    private String programName;
    private long runtime;                                                      // Sum of the durations of the initial solution and optimization algorithms
    private Map<String, ScheduledLesson> scheduledLessonMap = new HashMap<>(); // ClassId : ScheduledLesson
    private InMemoryRepository dataModel;
    private Set<Constraint> involvedConstraints = new HashSet<>();             // Contains the constraints of the classes

    private boolean updateCost = true;
    private int cost;

    public Timetable(String programName) {
        this.programName = programName;
    }

    public Timetable() {}

    public void bindDataModel(InMemoryRepository model) {
        this.dataModel = model;

        // Update constraints with already stored classes
        scheduledLessonMap.values().forEach(this::updateInvolvedConstraints);

        updateCost = true;
    }

    private void updateInvolvedConstraints(ScheduledLesson lesson) {
        if(dataModel == null) {
            throw new IllegalStateException("ScheduledClassService should have already been bound!");
        }

        String classId = lesson.getClassId();

        ClassUnit classUnit = dataModel.getClassUnit(classId);

        if (classUnit == null) {
            throw new RuntimeException("Class Unit " + classId + " couldn't be found");
        }

        involvedConstraints.addAll(classUnit.getConstraintList());
    }

    public List<ScheduledLesson> getScheduledLessonList() {
        return new ArrayList<>(scheduledLessonMap.values());
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getProgramName() {
        return programName;
    }

    public void setDateOfCreation(LocalDateTime dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    public long getRuntime() {
        return runtime;
    }

    public String getDateOfCreation() {
        return dateOfCreationFormatter.format(dateOfCreation).replace(' ', '_').replace(':', '.');
    }

    public void addScheduledLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonMap.put(scheduledLesson.getClassId(), scheduledLesson);

        if(dataModel != null) {
            updateInvolvedConstraints(scheduledLesson);
        }

        updateCost = true;
    }

    public ScheduledLesson getScheduledLesson(String classId) {
        return scheduledLessonMap.get(classId);
    }

    public int cost() {
        if(updateCost) {
            cost = 0;

            // Add the Time and Room penalties
            for (ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
                cost += scheduledLesson.toInt();
            }

            // Add the soft constraint penalties (only if they were violated)
            for (Constraint c : involvedConstraints) {
                cost += c.computePenalties(this);
            }

            updateCost = false;
        }

        return cost;
    }

    public boolean isValid() {
        List<String> conflicts = new ArrayList<>();

        for(Constraint c : involvedConstraints) {
            if(c.required) {
                c.getConflictingClasses(this, (classes) -> Collections.addAll(conflicts, classes));
            }
        }

        return conflicts.isEmpty();
    }

    @Override
    public String getTableName() {
        return "Timetables";
    }

    @Override
    public List<String> getColumnNames() {
        return List.of("Date of Creation", "Runtime (s)", "Cost", "Nº of Scheduled Lessons", "Is valid");
    }

    @Override
    public List<Object> getColumnValues() {
        return List.of(dateOfCreation.format(dateOfCreationFormatter), runtime, cost(), scheduledLessonMap.size(), isValid());
    }

    @Override
    public Timetable clone() {
        try {
            Timetable clone = (Timetable) super.clone();
            clone.programName = programName;
            clone.dateOfCreation = dateOfCreation;
            clone.dataModel = dataModel;
            clone.involvedConstraints = involvedConstraints;
            clone.scheduledLessonMap = new HashMap<>(scheduledLessonMap);
            clone.updateCost = updateCost;
            clone.cost = cost;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
