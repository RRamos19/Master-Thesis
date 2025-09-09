package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TimetableConfiguration;

import java.util.List;

public class MinGapConstraint extends Constraint {
    public MinGapConstraint(String restrictionType, String param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(restrictionType, penalty, required, Integer.valueOf(param1), timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        int G = firstParam;

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            String scheduledClass1Id = scheduledLesson1.getClassId();
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

                action.apply(scheduledClass1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
