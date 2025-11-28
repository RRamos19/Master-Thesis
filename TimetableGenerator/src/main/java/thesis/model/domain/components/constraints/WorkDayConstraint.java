package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class WorkDayConstraint extends Constraint {
    public WorkDayConstraint(int id, String restrictionType, int param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, restrictionType, penalty, required, param1, timetableConfiguration, PenaltyTypes.ConstraintCategory.TIME);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public ConstraintResults computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int scheduledClassesSize = scheduledClasses.size();
        final int S = getFirstParameter();
        int conflicts = 0;

        ConstraintResults results = new ConstraintResults();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            int scheduledLesson1Start = scheduledLesson1.getStartSlot();
            int scheduledLesson1End = scheduledLesson1.getEndSlot();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);
                int scheduledLesson2Start = scheduledLesson2.getStartSlot();
                int scheduledLesson2End = scheduledLesson2.getEndSlot();

                if((scheduledLesson1.getDays() & scheduledLesson2.getDays()) == 0 ||
                   (scheduledLesson1.getWeeks() & scheduledLesson2.getWeeks()) == 0 ||
                   Math.max(scheduledLesson1End, scheduledLesson2End) - Math.min(scheduledLesson1Start, scheduledLesson2Start) <= S) {
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
