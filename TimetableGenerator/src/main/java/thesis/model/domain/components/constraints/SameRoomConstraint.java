package thesis.model.domain.components.constraints;

import thesis.model.domain.components.*;

import java.util.List;

public class SameRoomConstraint extends Constraint {
    SameRoomConstraint(int id, String type, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(id, type, penalty, required, timetableConfiguration, PenaltyTypes.ConstraintCategory.ROOM);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    public ConstraintResults computePenalties(Timetable solution) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int scheduledClassesSize = scheduledClasses.size();
        int conflicts = 0;

        ConstraintResults results = new ConstraintResults();

        for(int i=0; i<scheduledClassesSize-1; i++) {
            ScheduledLesson scheduledLesson1 = scheduledClasses.get(i);
            String scheduledLesson1Room = scheduledLesson1.getRoomId();

            for(int j=i+1; j<scheduledClassesSize; j++) {
                ScheduledLesson scheduledLesson2 = scheduledClasses.get(j);

                if(scheduledLesson1Room.equals(scheduledLesson2.getRoomId())){
                    continue;
                }

                conflicts++;
            }

            if(conflicts == 0) {
                break;
            }
        }

        results.penalty = getRequired() ? conflicts : conflicts * getPenalty();
        results.conflictingClasses = this.getClassUnitIdList();

        return results;
    }
}
