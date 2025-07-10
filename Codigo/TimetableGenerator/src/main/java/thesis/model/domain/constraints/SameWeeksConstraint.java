package thesis.model.domain.constraints;

import thesis.model.domain.Constraint;
import thesis.model.domain.DomainModel;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SameWeeksConstraint extends Constraint {
    public SameWeeksConstraint(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public Set<String> getConflictingClasses(DomainModel model, Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);

                int orWeeks = scheduledLesson1.getWeeks() | scheduledLesson2.getWeeks();

                if(orWeeks == scheduledLesson1.getWeeks() || orWeeks == scheduledLesson2.getWeeks()){
                    continue;
                }

                conflictingClasses.add(scheduledLesson1.getClassId());
                conflictingClasses.add(scheduledLesson2.getClassId());
            }
        }

        return conflictingClasses;
    }
}
