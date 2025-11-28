package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class DifferentDaysConstraint extends Constraint {
    public DifferentDaysConstraint(int id, String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, type, penalty, required, timetableConfiguration, PenaltyTypes.ConstraintCategory.TIME);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public ConstraintResults computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledLessons = this.getScheduledClasses(solution);
        final int scheduledLessonsSize = scheduledLessons.size();
        int conflicts = 0;

        ConstraintResults results = new ConstraintResults();

        for(int i=0; i < scheduledLessonsSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledLessons.get(i);

            for(int j=i+1; j < scheduledLessonsSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledLessons.get(j);

                if((scheduledLesson1.getDays() & scheduledLesson2.getDays()) == 0) {
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
