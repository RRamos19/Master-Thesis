package thesis.model.domain.constraints;

import javafx.util.Pair;
import thesis.model.domain.Constraint;
import thesis.model.domain.DomainModel;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxBreaksConstraint extends Constraint {
    public MaxBreaksConstraint(String restrictionType, String param1, String param2, Integer penalty, boolean required) {
        super(restrictionType, penalty, required, Integer.parseInt(param1), Integer.parseInt(param2));
    }

    @Override
    public Set<String> getConflictingClasses(DomainModel model, Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);

            int scheduledLesson1Start = scheduledLesson1.getStartSlot();
            int scheduledLesson1Length = scheduledLesson1.getLength();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);

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
