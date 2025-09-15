package thesis.model.domain.constraints;

import thesis.model.domain.Constraint;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotOverlapConstraint extends Constraint {
    public NotOverlapConstraint(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
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

                if(scheduledLesson1Start+scheduledLesson1Length <= scheduledLesson2Start ||
                   scheduledLesson2Start+scheduledLesson2Length <= scheduledLesson1Start ||
                   (scheduledLesson1.getDays() & scheduledLesson2.getDays()) == 0 ||
                   (scheduledLesson1.getWeeks() & scheduledLesson2.getWeeks()) == 0){
                    continue;
                }

                conflictingClasses.add(scheduledLesson1.getClassId());
                conflictingClasses.add(scheduledLesson2.getClassId());
            }
        }

        return conflictingClasses;
    }
}
