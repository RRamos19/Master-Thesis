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
    public int computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledLessons = this.getScheduledClasses(solution);
        final int scheduledLessonsSize = scheduledLessons.size();
        int conflicts = 0;

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

        return getRequired() ? conflicts : conflicts * getPenalty();
    }
}
