package thesis.service;

import thesis.model.domain.ClassUnit;
import thesis.model.domain.DomainModel;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.util.List;

/**
 * The solution generator presented is based on Tomáš Müller's and Roman Barták's paper: Interactive Timetabling https://www.unitime.org/papers/it02_ctp.pdf
 */
public class MullerSolutionGenerator implements InitialSolutionGenerator<Timetable, DomainModel> {
    private boolean interruptAlgorithm = false;

    public Timetable generate(DomainModel data, int maxIterations) {
        Timetable solution = new Timetable(data.getProblemName());
        List<ClassUnit> unscheduled = data.getClassUnits();

        int iter = 0;
        while(!solution.isScheduleComplete() && iter < maxIterations && !interruptAlgorithm) {
            iter++;
            String classId = activitySelection(unscheduled);
            ScheduledLesson location = locationSelection();
            // TODO: confirmar

            solution.addScheduledLesson(location);
        }

        return solution;
    }

    private String activitySelection(List<ClassUnit> unscheduled) {
        // Returns the classId of a selected class
        // TODO: por implementar
        return null;
    }

    private ScheduledLesson locationSelection() {
        // Timetable.AssignedClass assignedClass = new Timetable.AssignedClass();
        // TODO: por implementar
        return null;
    }

    private void stopAlgorithm() {
        interruptAlgorithm = true;
    }
}
