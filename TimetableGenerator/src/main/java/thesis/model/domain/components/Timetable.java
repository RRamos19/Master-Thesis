package thesis.model.domain.components;

import thesis.model.domain.InMemoryRepository;
import thesis.model.parser.XmlResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Timetable implements XmlResult {
    private static final DateTimeFormatter dateOfCreationFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final UUID timetableId; // Useful for the database synchronization (to easily differentiate timetables from each other)

    private LocalDateTime dateOfCreation;
    private String programName;
    private long runtime;                                                            // Sum of the durations of the initial solution and optimization algorithms
    private final Map<String, ScheduledLesson> scheduledLessonMap = new HashMap<>(); // ClassId : ScheduledLesson
    private InMemoryRepository dataModel;

    // Reserved for temporary classes (which are used to check conflicts)
    private boolean hasTemporaryLesson = false;
    private ScheduledLesson originalLesson;

    // Cache of constraints
    private Set<Constraint> constraintSet;
    private boolean updateConstraints = true;

    // Cache of validation and cost
    private Boolean isValid;
    private boolean updateCost = true;
    private PenaltySum cost;

    public Timetable(UUID id, String programName, LocalDateTime dateOfCreation) {
        this.timetableId = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.programName = programName;
        this.dateOfCreation = dateOfCreation;
    }

    public Timetable(String programName, LocalDateTime dateOfCreation) {
        this(null, programName, dateOfCreation);
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
        updateCost = true;
        updateConstraints = true;
    }

    public UUID getTimetableId() {
        return timetableId;
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

        isValid = null;
        updateCost = true;
        updateConstraints = true;
    }

    public void addTemporaryLesson(ScheduledLesson scheduledLesson) {
        if(hasTemporaryLesson) {
            throw new IllegalStateException("Timetable: Only one temporary lesson should be added at a time!");
        }
        originalLesson = scheduledLessonMap.put(scheduledLesson.getClassId(), scheduledLesson);

        if(dataModel != null) {
            scheduledLesson.bindModel(dataModel);
        }

        isValid = null;
        updateCost = true;
        updateConstraints = true;
        hasTemporaryLesson = true;
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

    public PenaltySum cost() {
        if(updateCost) {
            int timePenalty = 0;
            int roomPenalty = 0;

            // Add the Time and Room penalties
            for (ScheduledLesson scheduledLesson : scheduledLessonMap.values()) {
                PenaltySum scheduledLessonPenalties = scheduledLesson.toInt();
                timePenalty += scheduledLessonPenalties.getTimePenalty();
                roomPenalty += scheduledLessonPenalties.getRoomPenalty();
            }

            // Add the soft constraint penalties
            int roomConstraintCost = 0;
            int timeConstraintCost = 0;
            int commonConstraintCost = 0;
            for(Constraint c : getConstraintSet()) {
                switch(c.getConstraintCategory()) {
                    case TIME:
                        timeConstraintCost += c.computePenalties(this).penalty;
                        break;
                    case ROOM:
                        roomConstraintCost += c.computePenalties(this).penalty;
                        break;
                    case COMMON:
                        commonConstraintCost += c.computePenalties(this).penalty;
                        break;
                    default:
                        throw new RuntimeException("Constraint category " + c.getConstraintCategory() + " unsupported");
                }
            }
            roomConstraintCost = roomConstraintCost * dataModel.getTimetableConfiguration().getDistribWeight();
            timeConstraintCost = timeConstraintCost * dataModel.getTimetableConfiguration().getDistribWeight();
            commonConstraintCost = commonConstraintCost * dataModel.getTimetableConfiguration().getDistribWeight();

            cost = new PenaltySum(roomPenalty + roomConstraintCost, timePenalty + timeConstraintCost, commonConstraintCost);
            updateCost = false;
        }

        return cost;
    }

    public boolean isValid() {
        if(isValid == null) {
            for(Constraint c : getConstraintSet()) {
                if(c.getRequired() && c.computePenalties(this).penalty != 0) {
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
