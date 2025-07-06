package thesis.model.domain.constraints;

import thesis.model.domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxDayLoadConstraint extends Constraint {
    public MaxDayLoadConstraint(String restrictionType, String param1, Integer penalty, boolean required) {
        super(restrictionType, penalty, required, Integer.valueOf(param1));
    }

    @Override
    public Set<String> getConflictingClasses(DomainModel model, Timetable solution) {
        Set<String> conflictingClasses = new HashSet<>();
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        int S = firstParam;
        TimetableConfiguration timetableConfiguration = model.getTimetableConfiguration();
        int nrWeeks = timetableConfiguration.getNumWeeks();
        short nrDays = timetableConfiguration.getNumDays();
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

        return conflictingClasses;
    }
}
