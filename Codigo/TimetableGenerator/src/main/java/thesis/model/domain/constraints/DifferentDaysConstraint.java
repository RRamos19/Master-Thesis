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

public class DifferentDaysConstraint extends Constraint {
    public DifferentDaysConstraint(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public Set<String> getConflictingClasses(DomainModel model, Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(i);

                if((scheduledLesson1.getDays() & scheduledLesson2.getDays()) == 0) {
                    continue;
                }

                conflictingClasses.add(scheduledLesson1.getClassId());
                conflictingClasses.add(scheduledLesson2.getClassId());
            }
        }

        return conflictingClasses;
    }
}
