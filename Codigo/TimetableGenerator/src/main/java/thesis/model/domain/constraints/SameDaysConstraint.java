package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.List;

public class SameDaysConstraint extends Constraint {
    public SameDaysConstraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
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
            Time time1 = scheduledLesson1.getScheduledTime();
            String scheduledClass1Id = scheduledLesson1.getClassId();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);
                Time time2 = scheduledLesson2.getScheduledTime();

                int orDays = time1.getDays() | time2.getDays();

                if (orDays == time1.getDays() || orDays == time2.getDays()) {
                    continue;
                }

                action.apply(scheduledClass1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
