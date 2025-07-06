package thesis.model.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Constraint {
    protected final String type;
    protected final Integer penalty;
    protected final boolean required;
    protected final List<String> classUnitIdList = new ArrayList<>();
    protected final Integer firstParam;
    protected final Integer secondParam;

    public Constraint(String type, Integer penalty, boolean required, Integer firstParam, Integer secondParam) {
        this.type = type;
        this.penalty = penalty;
        this.required = required;
        this.firstParam = firstParam;
        this.secondParam = secondParam;
    }

    public Constraint(String type, Integer penalty, boolean required) {
        this(type, penalty, required, null, null);
    }

    public Constraint(String type, Integer penalty, boolean required, Integer firstTimeslot) {
        this(type, penalty, required, firstTimeslot, null);
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
        return classUnitIdList;
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

    public void computeConflicts(ScheduledLesson lessonToSchedule, Timetable currentSolution, Set<String> classConflicts) {
        if(required) {
            // Create a timetable with lessonToSchedule already scheduled
            Timetable currentSolutionClone = currentSolution.clone();
            currentSolutionClone.addScheduledLesson(lessonToSchedule);

            // Get all the conflicts present in said timetable
            Set<String> conflicts = getConflictingClasses(lessonToSchedule.getModel(), currentSolutionClone);

            // Add all the conflicts to the set provided
            classConflicts.addAll(conflicts);
        }
    }


    //public abstract int getConflictPenalty(ScheduledLesson scheduledLesson, Timetable currentSolution);


    public abstract Set<String> getConflictingClasses(DomainModel model, Timetable solution);
}
