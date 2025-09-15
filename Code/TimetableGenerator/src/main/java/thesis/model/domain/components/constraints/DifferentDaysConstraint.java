package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TimetableConfiguration;

import java.util.List;

public class DifferentDaysConstraint extends Constraint {
    public DifferentDaysConstraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(type, penalty, required, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
        List<ScheduledLesson> scheduledLessons = this.getScheduledClasses(solution);
        int scheduledLessonsSize = scheduledLessons.size();

        for(int i=0; i < scheduledLessonsSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledLessons.get(i);
            String scheduledLesson1Id = scheduledLesson1.getClassId();

            for(int j=i+1; j < scheduledLessonsSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledLessons.get(j);

                if((scheduledLesson1.getDays() & scheduledLesson2.getDays()) == 0) {
                    continue;
                }

                action.apply(scheduledLesson1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
