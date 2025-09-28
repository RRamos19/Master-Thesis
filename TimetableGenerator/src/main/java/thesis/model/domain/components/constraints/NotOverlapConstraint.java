package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class NotOverlapConstraint extends Constraint {
    public NotOverlapConstraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(type, penalty, required, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public int computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        int scheduledClassesSize = scheduledClasses.size();
        int conflicts = 0;

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            Time scheduledLesson1Time = scheduledLesson1.getScheduledTime();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);

                if(!scheduledLesson1Time.overlaps(scheduledLesson2.getScheduledTime())){
                    continue;
                }

                conflicts++;
            }
        }

        return getRequired() ? conflicts : conflicts * getPenalty();
    }
}
