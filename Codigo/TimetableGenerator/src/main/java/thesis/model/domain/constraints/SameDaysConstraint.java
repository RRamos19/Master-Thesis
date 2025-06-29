package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SameDaysConstraint extends Constraint {
    public SameDaysConstraint(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public Set<String> getConflictingClasses(Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<String> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = solution.getScheduledLesson(scheduledClasses.get(i));
            Time time1 = scheduledLesson1.getScheduledTime();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = solution.getScheduledLesson(scheduledClasses.get(j));
                Time time2 = scheduledLesson2.getScheduledTime();

                int orDays = time1.getDays() | time2.getDays();

                if (orDays == time1.getDays() || orDays == time2.getDays()) {
                    continue;
                }

                conflictingClasses.add(scheduledLesson1.getClassId());
                conflictingClasses.add(scheduledLesson2.getClassId());
            }
        }


        return conflictingClasses;
    }
}
