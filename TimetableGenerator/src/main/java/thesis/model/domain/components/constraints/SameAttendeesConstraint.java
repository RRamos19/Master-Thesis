package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.*;

public class SameAttendeesConstraint extends Constraint {
    public SameAttendeesConstraint(int id, String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, type, penalty, required, timetableConfiguration, PenaltyTypes.ConstraintCategory.COMMON);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public ConstraintResults computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int scheduledClassesSize = scheduledClasses.size();
        int conflicts = 0;
        Set<String> classConflicts = new HashSet<>();

        ConstraintResults results = new ConstraintResults();

        for (int i = 0; i < scheduledClassesSize - 1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            String scheduledLesson1Id = scheduledLesson1.getClassId();
            Room room1 = scheduledLesson1.getRoom();
            Time time1 = scheduledLesson1.getScheduledTime();

            for (int j = i + 1; j < scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);
                Room room2 = scheduledLesson2.getRoom();
                Time time2 = scheduledLesson2.getScheduledTime();

                int travel = 0;
                if (room1 != null && room2 != null) {
                    travel = room1.getRoomDistance(room2.getIntId());
                }

                if (time1.getEndSlot() + travel <= time2.getStartSlot() ||
                        time2.getEndSlot() + travel <= time1.getStartSlot() ||
                        (time1.getDays() & time2.getDays()) == 0 ||
                        (time1.getWeeks() & time2.getWeeks()) == 0) {

                    continue;
                }

                conflicts++;
                classConflicts.add(scheduledLesson1Id);
                classConflicts.add(scheduledLesson2.getClassId());
            }
        }

        results.penalty = getRequired() ? conflicts : conflicts * getPenalty();
        results.conflictingClasses = classConflicts;

        return results;
    }
}
