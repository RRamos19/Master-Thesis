package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TimetableConfiguration;

import java.util.List;

public class MaxDayLoadConstraint extends Constraint {
    public MaxDayLoadConstraint(String restrictionType, Integer param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(restrictionType, penalty, required, param1, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public int computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int S = getFirstParameter();
        final int nrWeeks = getNrWeeks();
        int sum = 0;

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

        return getRequired()
                ? sum > 0 ? Math.max(1, sum / nrWeeks) : 0
                : getPenalty() * sum / nrWeeks;
    }
}
