package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TimetableConfiguration;

import java.util.List;

public class MinGapConstraint extends Constraint {
    public MinGapConstraint(String restrictionType, Integer param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(restrictionType, penalty, required, param1, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public int computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int G = getFirstParameter();
        final int scheduledClassesSize = scheduledClasses.size();
        int conflicts = 0;

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            int scheduledLesson1Start = scheduledLesson1.getStartSlot();
            int scheduledLesson1End = scheduledLesson1.getEndSlot();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);
                int scheduledLesson2Start = scheduledLesson2.getStartSlot();
                int scheduledLesson2End = scheduledLesson2.getEndSlot();

                if ((scheduledLesson1.getDays() & scheduledLesson2.getDays()) == 0 ||
                    (scheduledLesson1.getWeeks() & scheduledLesson2.getWeeks()) == 0 ||
                    scheduledLesson1End + G <= scheduledLesson2Start ||
                    scheduledLesson2End + G <= scheduledLesson1Start) {
                    continue;
                }

                conflicts++;
            }
        }

        return getRequired() ? conflicts : conflicts * getPenalty();
    }
}
