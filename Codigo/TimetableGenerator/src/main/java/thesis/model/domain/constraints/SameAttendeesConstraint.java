package thesis.model.domain.constraints;

import javafx.util.Pair;
import thesis.model.domain.Constraint;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SameAttendeesConstraint extends Constraint {
    public SameAttendeesConstraint(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public void computeConflicts(String cls, Set<String> classConflicts) {
        // TODO: por fazer
    }

    @Override
    public List<Pair<String, String>> getConflictingClasses(Timetable solution) {
        List<Pair<String, String>> conflictingClasses = new ArrayList<>();
        List<String> scheduledClasses = this.getScheduledClasses(solution);

        // There can only be a conflict if there are two or more classes present in this
        // restriction that are scheduled
        if(scheduledClasses.size() >= 2) {

        }

        return conflictingClasses;
    }
}
