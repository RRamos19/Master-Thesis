package thesis.model.domain.components;

import thesis.model.domain.InMemoryRepository;
import thesis.model.parser.XmlResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Timetable implements TableDisplayable, XmlResult {
    private static final DateTimeFormatter dateOfCreationFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
    private LocalDateTime dateOfCreation;
    private String programName;
    private long runtime;                                                            // Sum of the durations of the initial solution and optimization algorithms
    private final Map<String, ScheduledLesson> scheduledLessonMap = new HashMap<>(); // ClassId : ScheduledLesson
    private InMemoryRepository dataModel;

    // Reserved for temporary classes (to check conflicts)
    private boolean hasTemporaryLesson = false;
    private ScheduledLesson originalLesson;

    // Cache of constraints
    private Set<Constraint> constraintSet;
    private boolean updateConstraints = true;

    // Cache of validation and cost
    private Boolean isValid;
    private boolean updateCost = true;
    private int cost;

    public Timetable(Timetable other) {
        this.dateOfCreation = other.dateOfCreation;
        this.programName = other.programName;
        this.runtime = other.runtime;
        this.scheduledLessonMap.putAll(other.scheduledLessonMap);
        this.dataModel = other.dataModel;
    }

    public Timetable(String programName, LocalDateTime dateOfCreation) {
        this.programName = programName;
        this.dateOfCreation = dateOfCreation;
    }

    public Timetable(String programName) {
        this(programName, LocalDateTime.now());
    }

    public Timetable() {
        this(null, LocalDateTime.now());
    }

    public void bindDataModel(InMemoryRepository model) {
        this.dataModel = model;

        updateCost = true;
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

    public LocalDate getLocalDateOfCreation() {
        return dateOfCreation.toLocalDate();
    }

    public String getDateOfCreation() {
        return dateOfCreationFormatter.format(dateOfCreation).replace(' ', '_').replace(':', '.');
    }

    public void addScheduledLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonMap.put(scheduledLesson.getClassId(), scheduledLesson);

        if(dataModel != null) {
            scheduledLesson.bindModel(dataModel);
        }

        updateCost = true;
        updateConstraints = true;
        isValid = null;
    }

    public void addTemporaryLesson(ScheduledLesson scheduledLesson) {
        if(hasTemporaryLesson) {
            throw new IllegalStateException("Timetable: Only one temporary lesson should be added at a time!");
        }
        originalLesson = scheduledLessonMap.put(scheduledLesson.getClassId(), scheduledLesson);

        if(dataModel != null) {
            scheduledLesson.bindModel(dataModel);
        }

        updateCost = true;
        hasTemporaryLesson = true;
        updateConstraints = true;
        isValid = null;
    }

    public void removeTemporaryLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonMap.remove(scheduledLesson.getClassId());

        if(originalLesson != null) {
            addScheduledLesson(originalLesson);
        }

        hasTemporaryLesson = false;
        updateConstraints = true;
        isValid = null;
    }

    public ScheduledLesson getScheduledLesson(String classId) {
        return scheduledLessonMap.get(classId);
    }

    public Set<Constraint> getConstraintSet() {
        if(updateConstraints) {
            constraintSet = new HashSet<>();

            for (ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
                ClassUnit cls = scheduledLesson.getClassUnit();
                if (cls == null) {
                    throw new IllegalStateException("Timetable: ClassUnit of scheduled lesson is null!");
                }
                constraintSet.addAll(cls.getConstraintList());
            }

            updateConstraints = false;
        }

        return constraintSet;
    }

    public int cost() {
        if(updateCost) {
            cost = 0;

            // Add the Time and Room penalties
            for (ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
                cost += scheduledLesson.toInt();
            }

            // Add the soft constraint penalties
            int constraintCost = 0;
            for(Constraint c : getConstraintSet()) {
                constraintCost += c.computePenalties(this);
            }
            cost += constraintCost * dataModel.getTimetableConfiguration().getDistribWeight();

            updateCost = false;
        }

        return cost;
    }

    public boolean isValid() {
        if(isValid == null) {
            for(Constraint c : getConstraintSet()) {
                if(c.getRequired() && c.computePenalties(this) != 0) {
                    isValid = false;
                    break;
                }
            }

            if(isValid == null) isValid = true;
        }

        return isValid;
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
    public boolean isOptimizable() {
        return true;
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Timetable)) return false;
        Timetable timetable = (Timetable) o;
        return Objects.equals(programName, timetable.programName) && Objects.equals(scheduledLessonMap, timetable.scheduledLessonMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programName, scheduledLessonMap);
    }
}
