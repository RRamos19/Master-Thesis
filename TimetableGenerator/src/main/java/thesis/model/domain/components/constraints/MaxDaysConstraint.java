package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxDaysConstraint extends Constraint {
    public MaxDaysConstraint(int id, String restrictionType, Integer param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, restrictionType, penalty, required, param1, timetableConfiguration, PenaltyTypes.ConstraintCategory.TIME);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public ConstraintResults computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int D = getFirstParameter();
        int acc = 0;

        ConstraintResults results = new ConstraintResults();
        results.penalty = 0;
        results.conflictingClasses = this.getClassUnitIdList();

        for (ScheduledLesson scheduledLesson : scheduledClasses) {
            acc |= scheduledLesson.getDays();
        }

        var count = Integer.bitCount(acc);
        if (count <= D)
        {
            return results;
        }

        results.penalty = getRequired() ? count : count * getPenalty();

        return results;
    }
}
