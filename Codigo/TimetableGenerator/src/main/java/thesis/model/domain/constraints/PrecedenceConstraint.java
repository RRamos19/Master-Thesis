package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.List;

public class PrecedenceConstraint extends Constraint {
    public PrecedenceConstraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
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

                if(scheduledLesson2.getScheduledTime().isEarlier(scheduledLesson1.getScheduledTime())){
                    continue;
                }

                action.apply(scheduledClass1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
