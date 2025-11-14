package thesis.model.domain.components;

import thesis.model.domain.InMemoryRepository;
import thesis.model.parser.XmlResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Timetable implements XmlResult {
    private static final DateTimeFormatter dateOfCreationFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
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

        for(ScheduledLesson lesson : scheduledLessonMap.values()) {
            // Bind the lesson to the model if this solution was read from a file
            if (lesson.getModel() == null) {
                lesson.bindModel(model);
            }
        }

        isValid = null;
        updateConstraints = true;
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

    public LocalDateTime getDateOfCreation() {
        return dateOfCreation;
    }

    public String getDateOfCreationString() {
        return dateOfCreationFormatter.format(dateOfCreation);
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

        updateCost = true;
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
                    System.out.println(c.getType());
                    System.out.println(c.getClassUnitIdList());
                    System.out.println(c.EvaluateConflictingClasses(this));
                    isValid = false;
                    break;
                }
            }

            if(isValid == null) isValid = true;
        }

        return isValid;
    }

    public void clearCache() {
        isValid = null;
        updateConstraints = true;
        updateCost = true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Timetable)) return false;
        Timetable timetable = (Timetable) o;
        return Objects.equals(programName, timetable.programName) &&
                Objects.equals(dateOfCreation, timetable.dateOfCreation) &&
                Objects.equals(scheduledLessonMap, timetable.scheduledLessonMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programName, dateOfCreation, scheduledLessonMap);
    }
}
