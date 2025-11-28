package thesis.model.domain.components;

import java.util.*;

public abstract class Constraint {
    private final int id; // Used to differentiate constraints from each other

    private final String type;
    private final Integer penalty;
    private final boolean required;
    private final Set<String> classUnitIdList = new HashSet<>();
    private final Integer firstParam;
    private final Integer secondParam;
    private final int nrWeeks;
    private final short nrDays;
    private final PenaltyTypes.ConstraintCategory constraintCategory;

    public Constraint(int id, String type, Integer penalty, boolean required, Integer firstParam, Integer secondParam, TimetableConfiguration timetableConfiguration, PenaltyTypes.ConstraintCategory constraintCategory) {
        this.id = id;
        this.type = type;
        this.penalty = penalty;
        this.required = required;
        this.firstParam = firstParam;
        this.secondParam = secondParam;
        this.nrWeeks = timetableConfiguration.getNumWeeks();
        this.nrDays = timetableConfiguration.getNumDays();
        this.constraintCategory = constraintCategory;
    }

    public Constraint(int id, String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration, PenaltyTypes.ConstraintCategory constraintCategory) {
        this(id, type, penalty, required, null, null, timetableConfiguration, constraintCategory);
    }

    public Constraint(int id, String type, Integer penalty, boolean required, Integer firstTimeslot, TimetableConfiguration timetableConfiguration, PenaltyTypes.ConstraintCategory constraintCategory) {
        this(id, type, penalty, required, firstTimeslot, null, timetableConfiguration, constraintCategory);
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public boolean getRequired() {
        return required;
    }

    public Integer getFirstParameter() {
        return firstParam;
    }

    protected int getNrWeeks() {
        return nrWeeks;
    }

    protected short getNrDays() {
        return nrDays;
    }

    public Integer getSecondParameter() {
        return secondParam;
    }

    public void addClassUnitId(String classUnitId) {
        classUnitIdList.add(classUnitId);
    }

    public Set<String> getClassUnitIdList() {
        return Collections.unmodifiableSet(classUnitIdList);
    }

    public PenaltyTypes.ConstraintCategory getConstraintCategory() {
        return constraintCategory;
    }

    /**
     * Obtain the classes that are already scheduled and are present in this restriction
     * @param solution
     * @return A list of the class ids that are scheduled and present in this restriction
     */
    protected List<ScheduledLesson> getScheduledClasses(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = new ArrayList<>();
        Set<String> classUnitList = this.getClassUnitIdList();

        for(ScheduledLesson scheduledLesson : solution.getScheduledLessonList()) {
            String classId = scheduledLesson.getClassId();
            if(classUnitList.contains(classId)) {
                scheduledClasses.add(scheduledLesson);
            }
        }

        return scheduledClasses;
    }

    public abstract ConstraintResults computePenalties(Timetable solution);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Constraint)) return false;
        Constraint that = (Constraint) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
