package thesis.model.domain.elements.constraints;

import thesis.model.domain.elements.Constraint;
import thesis.model.domain.elements.ScheduledLesson;
import thesis.model.domain.elements.Timetable;
import thesis.model.domain.elements.TimetableConfiguration;

import java.util.List;

public class SameStartConstraint extends Constraint {
    public SameStartConstraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
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
            int scheduledLesson1Start = scheduledLesson1.getStartSlot();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);
                int scheduledLesson2Start = scheduledLesson2.getStartSlot();

                if(scheduledLesson1Start == scheduledLesson2Start){
                    continue;
                }

                action.apply(scheduledClass1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
