package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class SameDaysConstraint extends Constraint {
    public SameDaysConstraint(int id, String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, type, penalty, required, timetableConfiguration, PenaltyTypes.ConstraintCategory.TIME);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public ConstraintResults computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        int scheduledClassesSize = scheduledClasses.size();
        int conflicts = 0;

        ConstraintResults results = new ConstraintResults();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            Time time1 = scheduledLesson1.getScheduledTime();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);
                Time time2 = scheduledLesson2.getScheduledTime();

                int orDays = time1.getDays() | time2.getDays();

                if ((orDays == time1.getDays()) || (orDays == time2.getDays())) {
                    continue;
                }

                conflicts++;
            }
        }

        results.penalty = getRequired() ? conflicts : conflicts * getPenalty();
        results.conflictingClasses = this.getClassUnitIdList();

        return results;
    }
}
