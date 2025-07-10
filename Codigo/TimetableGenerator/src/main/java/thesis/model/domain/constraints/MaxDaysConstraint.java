package thesis.model.domain.constraints;

import thesis.model.domain.Constraint;
import thesis.model.domain.DomainModel;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxDaysConstraint extends Constraint {
    public MaxDaysConstraint(String restrictionType, String param1, Integer penalty, boolean required) {
        super(restrictionType, penalty, required, Integer.valueOf(param1));
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public Set<String> getConflictingClasses(DomainModel model, Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        int D = firstParam;
        int acc = 0;

        for (ScheduledLesson scheduledLesson : scheduledClasses) {
            acc |= scheduledLesson.getDays();

            conflictingClasses.add(scheduledLesson.getClassId());
        }

        var count = Integer.bitCount(acc);
        if (count <= D)
        {
            return new HashSet<>();
        }

        return conflictingClasses;
    }
}
