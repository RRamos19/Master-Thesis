package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxDaysConstraint extends Constraint {
    public MaxDaysConstraint(String restrictionType, String param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(restrictionType, penalty, required, Integer.valueOf(param1), timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
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
            return;
        }

        // If the count is larger than the max days then the action is applied
        conflictingClasses.forEach(action::apply);
    }
}
