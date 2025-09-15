package thesis.model.domain.constraints;

import thesis.model.domain.Constraint;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxDayLoadConstraint extends Constraint {
    public MaxDayLoadConstraint(String restrictionType, String param1, Integer penalty, boolean required) {
        super(restrictionType, penalty, required, Integer.valueOf(param1));
    }

    @Override
    public Set<String> getConflictingClasses(Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<String> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = solution.getScheduledLesson(scheduledClasses.get(i));

            int scheduledLesson1Start = scheduledLesson1.getStartSlot();
            int scheduledLesson1Length = scheduledLesson1.getLength();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = solution.getScheduledLesson(scheduledClasses.get(j));

                int scheduledLesson2Start = scheduledLesson2.getStartSlot();
                int scheduledLesson2Length = scheduledLesson2.getLength();

                if(true){
                    // TODO: falta terminar
                    continue;
                }

                conflictingClasses.add(scheduledLesson1.getClassId());
                conflictingClasses.add(scheduledLesson2.getClassId());
            }
        }

        return conflictingClasses;
    }
}
