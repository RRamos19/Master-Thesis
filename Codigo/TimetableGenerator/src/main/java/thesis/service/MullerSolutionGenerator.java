package thesis.service;

import thesis.model.aggregates.ScheduledClass;
import thesis.model.aggregates.StructuredTimetableData;
import thesis.model.entities.Subpart;
import thesis.model.entities.Timetable;

import java.util.List;

/**
 * The solution generator presented is based on Tomáš Müller's and Roman Barták's paper: Interactive Timetabling https://www.unitime.org/papers/it02_ctp.pdf
 */
public class MullerSolutionGenerator implements InitialSolutionGenerator<Timetable, StructuredTimetableData> {
    private boolean interruptAlgorithm = false;

    public Timetable generate(StructuredTimetableData data, int maxIterations) {
        Timetable solution = new Timetable();
        List<Subpart> unscheduled = data.getSubparts();

        int iter = 0;
        while(!solution.isScheduleComplete() && iter < maxIterations && !interruptAlgorithm) {
            iter++;
            String classId = activitySelection(unscheduled);
            ScheduledClass location = locationSelection();
            // TODO: confirmar
            solution.storeAssignedClass(location.getClassId(), location);
        }

        return solution;
    }

    private String activitySelection(List<Subpart> unscheduled) {
        // Returns the classId of a selected class
        // TODO: por implementar
        return null;
    }

    private ScheduledClass locationSelection() {
        // Timetable.AssignedClass assignedClass = new Timetable.AssignedClass();
        // TODO: por implementar
        return null;
    }

    private void stopAlgorithm() {
        interruptAlgorithm = true;
    }
}
