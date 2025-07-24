package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.List;

public class MaxDayLoadConstraint extends Constraint {
    public MaxDayLoadConstraint(String restrictionType, String param1, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(restrictionType, penalty, required, Integer.valueOf(param1), timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        int S = firstParam;
        int sum = 0;

        for(int week=0; week<nrWeeks; week++) {
            for(short day=0; day<nrDays; day++) {
                int dayLoad = 0;

                for (ScheduledLesson scheduledLesson : scheduledClasses) {
                    if ((scheduledLesson.getDays() & (1 << day)) != 0 &&
                        (scheduledLesson.getWeeks() & (1 << week)) != 0) {
                        dayLoad += scheduledLesson.getLength();
                    }
                }

                // TODO: falta adicionar os conflitos de aulas

                sum += Math.max(dayLoad - S, 0);
            }
        }
    }
}
