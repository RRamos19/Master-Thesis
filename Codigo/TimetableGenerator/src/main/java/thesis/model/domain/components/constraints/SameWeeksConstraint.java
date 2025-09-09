package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TimetableConfiguration;

import java.util.List;

public class SameWeeksConstraint extends Constraint {
    public SameWeeksConstraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(type, penalty, required, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            String scheduledClass1Id = scheduledLesson1.getClassId();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);

                int orWeeks = scheduledLesson1.getWeeks() | scheduledLesson2.getWeeks();

                if(orWeeks == scheduledLesson1.getWeeks() || orWeeks == scheduledLesson2.getWeeks()){
                    continue;
                }

                action.apply(scheduledClass1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
