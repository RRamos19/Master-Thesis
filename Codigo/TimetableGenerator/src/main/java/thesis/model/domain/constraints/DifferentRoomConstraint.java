package thesis.model.domain.constraints;

import thesis.model.domain.Constraint;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.util.*;

public class DifferentRoomConstraint extends Constraint {
    public DifferentRoomConstraint(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public Set<String> getConflictingClasses(Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<String> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = solution.getScheduledLesson(scheduledClasses.get(i));

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = solution.getScheduledLesson(scheduledClasses.get(j));

                if(!Objects.equals(scheduledLesson1.getRoomId(), scheduledLesson2.getRoomId())) {
                    continue;
                }

                conflictingClasses.add(scheduledLesson1.getClassId());
                conflictingClasses.add(scheduledLesson2.getClassId());
            }
        }

        return conflictingClasses;
    }
}
