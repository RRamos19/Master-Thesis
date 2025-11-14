package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TimetableConfiguration;

import java.util.List;

public class SameStartConstraint extends Constraint {
    public SameStartConstraint(int id, String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, type, penalty, required, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public int computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int scheduledClassesSize = scheduledClasses.size();
        int conflicts = 0;

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            int scheduledLesson1Start = scheduledLesson1.getStartSlot();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);

                if(scheduledLesson1Start == scheduledLesson2.getStartSlot()){
                    continue;
                }

                conflicts++;
            }

            if(conflicts == 0) {
                break;
            }
        }

        return getRequired() ? conflicts : conflicts * getPenalty();
    }
}
