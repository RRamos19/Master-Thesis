package thesis.model.domain.elements;

import java.util.*;

public abstract class Constraint {
    protected final String type;
    protected final Integer penalty;
    protected final boolean required;
    protected final List<String> classUnitIdList = new ArrayList<>();
    protected final Integer firstParam;
    protected final Integer secondParam;
    protected final short distribWeight;
    protected final int nrWeeks;
    protected final short nrDays;

    public Constraint(String type, Integer penalty, boolean required, Integer firstParam, Integer secondParam, TimetableConfiguration timetableConfiguration) {
        this.type = type;
        this.penalty = penalty;
        this.required = required;
        this.firstParam = firstParam;
        this.secondParam = secondParam;
        this.distribWeight = timetableConfiguration.getDistribWeight();
        this.nrWeeks = timetableConfiguration.getNumWeeks();
        this.nrDays = timetableConfiguration.getNumDays();
    }

    public Constraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        this(type, penalty, required, null, null, timetableConfiguration);
    }

    public Constraint(String type, Integer penalty, boolean required, Integer firstTimeslot, TimetableConfiguration timetableConfiguration) {
        this(type, penalty, required, firstTimeslot, null, timetableConfiguration);
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

    public Integer getSecondParameter() {
        return secondParam;
    }

    public void addClassUnitId(String classUnitId) {
        classUnitIdList.add(classUnitId);
    }

    public List<String> getClassUnitIdList() {
        return Collections.unmodifiableList(classUnitIdList);
    }

    /**
     * Obtain the classes that are already scheduled and are present in this restriction
     * @param solution
     * @return A list of the class ids that are scheduled and present in this restriction
     */
    protected List<ScheduledLesson> getScheduledClasses(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = new ArrayList<>();

        for(ScheduledLesson scheduledLesson : solution.getScheduledLessonList()) {
            String classId = scheduledLesson.getClassId();
            if(this.getClassUnitIdList().contains(classId)) {
                scheduledClasses.add(scheduledLesson);
            }
        }

        return scheduledClasses;
    }

    /**
     * Adds all the hard conflicts between classes if lessonToSchedule was scheduled to classConflicts
     * @param lessonToSchedule Lesson that is to be scheduled
     * @param currentSolution Current timetable (before lessonToSchedule is scheduled)
     * @param classConflicts Set of conflicting class ids
     */
    public void computeConflicts(ScheduledLesson lessonToSchedule, Timetable currentSolution, Set<String> classConflicts) {
        if(required) {
            // Create a timetable with lessonToSchedule already scheduled
            Timetable currentSolutionClone = currentSolution.clone();
            currentSolutionClone.addScheduledLesson(lessonToSchedule);

            Set<String> conflicts = new HashSet<>();

            // Get all the conflicts present in said timetable
            getConflictingClasses(currentSolutionClone, (conflictIds -> conflicts.addAll(Arrays.asList(conflictIds))));

            // Remove the class to be scheduled to obtain only the classes it conflicts with
            conflicts.remove(lessonToSchedule.getClassId());

            // Add all the conflicts to the set provided
            classConflicts.addAll(conflicts);
        }
    }

    /**
     * Adds all the soft constraint penalties if there are conflicts between classes
     * @param currentSolution Current timetable
     * @return Sum of penalties for a specific constraint
     */
    public int computePenalties(Timetable currentSolution) {
        // An array is used to allow the modification of the int inside the lambda function
        int[] penaltySum = {0};
        if(!required) {
            // Get all the conflicts present in a given timetable
            getConflictingClasses(currentSolution, (conflictIds) -> penaltySum[0] += penalty);
        }
        return penaltySum[0] * distribWeight;
    }

    @FunctionalInterface
    protected interface conflictAction {
        void apply(String ... conflictId);
    }

    protected abstract void getConflictingClasses(Timetable solution, conflictAction action);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Constraint)) return false;
        Constraint that = (Constraint) o;
        return required == that.required &&
                Objects.equals(type, that.type) &&
                Objects.equals(penalty, that.penalty) &&
                Objects.equals(classUnitIdList, that.classUnitIdList) &&
                Objects.equals(firstParam, that.firstParam) &&
                Objects.equals(secondParam, that.secondParam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, penalty, required, classUnitIdList, firstParam, secondParam);
    }
}
