package thesis.model.domain;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Constraint {
    protected final String type;
    protected final Integer penalty;
    protected final boolean required;
    protected final List<String> classUnitIdList = new ArrayList<>();
    protected final Integer firstTimeslotParameter;
    protected final Integer secondTimeslotParameter;

    public Constraint(String type, Integer penalty, boolean required, Integer firstTimeslot, Integer secondTimeslot) {
        this.type = type;
        this.penalty = penalty;
        this.required = required;
        this.firstTimeslotParameter = firstTimeslot;
        this.secondTimeslotParameter = secondTimeslot;
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

    public Integer getFirstTimeslotParameter() {
        return firstTimeslotParameter;
    }

    public Integer getSecondTimeslotParameter() {
        return secondTimeslotParameter;
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
    protected List<String> getScheduledClasses(Timetable solution) {
        List<String> scheduledClasses = new ArrayList<>();

        for(ScheduledLesson scheduledLesson : solution.getScheduledLessonList()) {
            String classId = scheduledLesson.getClassId();
            if(this.getClassUnitIdList().contains(classId)) {
                scheduledClasses.add(classId);
            }
        }

        return scheduledClasses;
    }

    public abstract void computeConflicts(String cls, Set<String> classConflicts);

    public abstract List<Pair<String, String>> getConflictingClasses(Timetable solution);
}
