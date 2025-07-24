package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.List;

public class MaxBreaksConstraint extends Constraint {
    public MaxBreaksConstraint(String restrictionType, String param1, String param2, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(restrictionType, penalty, required, Integer.parseInt(param1), Integer.parseInt(param2), timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);

            String scheduledClass1Id = scheduledLesson1.getClassId();
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

                action.apply(scheduledClass1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
