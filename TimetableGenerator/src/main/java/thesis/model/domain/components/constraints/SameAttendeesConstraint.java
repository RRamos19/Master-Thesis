package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class SameAttendeesConstraint extends Constraint {
    public SameAttendeesConstraint(String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(type, penalty, required, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            Room room1 = scheduledLesson1.getRoom();
            Time time1 = scheduledLesson1.getScheduledTime();
            String scheduledLesson1Id = scheduledLesson1.getClassId();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);
                Room room2 = scheduledLesson2.getRoom();
                Time time2 = scheduledLesson2.getScheduledTime();

                int travel = 0;
                if(room1 != null && room2 != null) {
                    travel = room1.getRoomDistance(room2.getIntId());
                }

                if (time1.getEndSlot() + travel <= time2.getStartSlot() ||
                    time2.getEndSlot() + travel <= time1.getStartSlot() ||
                    (time1.getDays() & time2.getDays()) == 0 ||
                    (time1.getWeeks() & time2.getWeeks()) == 0) {

                    continue;
                }

                action.apply(scheduledLesson1Id, scheduledLesson2.getClassId());
            }
        }
    }
}
