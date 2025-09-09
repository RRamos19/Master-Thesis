package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class PrecedenceConstraint extends Constraint {
    public PrecedenceConstraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(type, penalty, required, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
        List<ScheduledLesson> scheduledLessons = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledLessons.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledLessons.get(i);
            String scheduledLesson1Id = scheduledLesson1.getClassId();
            Time scheduledLesson1Time = scheduledLesson1.getScheduledTime();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledLessons.get(j);

                if(scheduledLesson2.getScheduledTime().isEarlier(scheduledLesson1Time)){
                    continue;
                }

                action.apply(scheduledLesson1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
