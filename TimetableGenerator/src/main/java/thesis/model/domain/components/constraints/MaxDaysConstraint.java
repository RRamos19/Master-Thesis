package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TimetableConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxDaysConstraint extends Constraint {
    public MaxDaysConstraint(String restrictionType, Integer param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(restrictionType, penalty, required, param1, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public int computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int D = getFirstParameter();
        int acc = 0;

        for (ScheduledLesson scheduledLesson : scheduledClasses) {
            acc |= scheduledLesson.getDays();
        }

        var count = Integer.bitCount(acc);
        if (count <= D)
        {
            return 0;
        }

        return getRequired() ? count : count * getPenalty();
    }
}
