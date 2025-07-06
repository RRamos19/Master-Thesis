package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotOverlapConstraint extends Constraint {
    public NotOverlapConstraint(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public Set<String> getConflictingClasses(DomainModel model, Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);

            Time scheduledLesson1Time = scheduledLesson1.getScheduledTime();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);

                if(!scheduledLesson1Time.overlaps(scheduledLesson2.getScheduledTime())){
                    continue;
                }

                conflictingClasses.add(scheduledLesson1.getClassId());
                conflictingClasses.add(scheduledLesson2.getClassId());
            }
        }

        return conflictingClasses;
    }
}
