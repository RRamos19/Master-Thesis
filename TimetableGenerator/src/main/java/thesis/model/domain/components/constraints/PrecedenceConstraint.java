package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class PrecedenceConstraint extends Constraint {
    public PrecedenceConstraint(int id, String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, type, penalty, required, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public int computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledLessons = this.getScheduledClasses(solution);
        final int scheduledClassesSize = scheduledLessons.size();
        int conflicts = 0;

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledLessons.get(i);
            Time scheduledLesson1Time = scheduledLesson1.getScheduledTime();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledLessons.get(j);

                if(scheduledLesson1Time.isEarlier(scheduledLesson2.getScheduledTime())){
                    continue;
                }

                conflicts++;
            }
        }

        return getRequired() ? conflicts : conflicts * getPenalty();
    }
}
