package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SameAttendeesConstraint extends Constraint {
    public SameAttendeesConstraint(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public Set<String> getConflictingClasses(DomainModel model, Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);

        int scheduledClassesSize = scheduledClasses.size();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            Room room1 = scheduledLesson1.getRoom();
            Time time1 = scheduledLesson1.getScheduledTime();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);
                Room room2 = scheduledLesson2.getRoom();
                Time time2 = scheduledLesson2.getScheduledTime();

                int travel = room1 != null && room2 != null ? room1.getRoomDistance(room2.getRoomId()) : 0;

                if (time1.getEndSlot() + travel <= time2.getStartSlot() ||
                    time2.getEndSlot() + travel <= time1.getStartSlot() ||
                    (time1.getDays() & time2.getDays()) == 0 ||
                    (time1.getWeeks() & time2.getWeeks()) == 0) {
                    continue;
                }

                conflictingClasses.add(scheduledLesson1.getClassId());
                conflictingClasses.add(scheduledLesson2.getClassId());
            }
        }


        return conflictingClasses;
    }
}
