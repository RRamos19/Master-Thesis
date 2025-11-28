package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class MaxDayLoadConstraint extends Constraint {
    public MaxDayLoadConstraint(int id, String restrictionType, Integer param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, restrictionType, penalty, required, param1, timetableConfiguration, PenaltyTypes.ConstraintCategory.TIME);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public ConstraintResults computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int S = getFirstParameter();
        final int nrWeeks = getNrWeeks();
        int sum = 0;

        ConstraintResults results = new ConstraintResults();

        for(int week=0; week < getNrWeeks(); week++) {
            for(short day=0; day < getNrDays(); day++) {
                int dayLoad = 0;

                for (ScheduledLesson scheduledLesson : scheduledClasses) {
                    if ((scheduledLesson.getDays() & (1 << day)) != 0 &&
                        (scheduledLesson.getWeeks() & (1 << week)) != 0) {
                        dayLoad += scheduledLesson.getLength();
                    }
                }

                sum += Math.max(dayLoad - S, 0);
            }
        }

        results.penalty = getRequired()
                ? sum > 0 ? Math.max(1, sum / nrWeeks) : 0
                : getPenalty() * sum / nrWeeks;
        results.conflictingClasses = this.getClassUnitIdList();

        return results;
    }
}
