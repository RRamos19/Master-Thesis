package thesis.solver.core;

import thesis.model.domain.Constraint;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Time;

import java.util.*;

public class DefaultISGModel implements ISGModel<DefaultISGValue, DefaultISGVariable, DefaultISGSolution> {
    private List<DefaultISGVariable> variableList;
    private List<DefaultISGVariable> unassignedVariableList;
    private List<DefaultISGVariable> bestUnassignedVariableList = null;
    private DefaultISGSolution solution;

    @Override
    public DefaultISGSolution createInitialSolution() {
        solution = new DefaultISGSolution(this);
        variableList = solution.getAssignedVariables();
        unassignedVariableList = solution.getUnassignedVariables();
        return solution;
    }

    public void setSolution(DefaultISGSolution solution) {
        this.solution = solution;
        this.variableList = solution.getAssignedVariables();
        this.unassignedVariableList = solution.getUnassignedVariables();
    }

    @Override
    public List<DefaultISGVariable> getBestUnassignedVariables() {
        return bestUnassignedVariableList;
    }

    @Override
    public Set<String> conflictValues(DefaultISGValue value) {
        HashSet<String> conflictIds = new HashSet<>();

        // Add the constraint conflicts
        for (Constraint constraint : value.variable().variable().getConstraintList()) {
            constraint.computeConflicts(value.value(), solution.solution(), conflictIds);
        }

        // Variables to avoid multiple method calls in the for loop
        ScheduledLesson valueLesson = value.value();
        String valueClassId = valueLesson.getClassId();
        String valueRoomId = valueLesson.getRoomId();
        Time valueTime = valueLesson.getScheduledTime();

        // Add the room conflicts
        for(DefaultISGVariable variable : solution.getAssignedVariables()) {
            ScheduledLesson scheduledLesson = variable.getAssignment().value();

            if(scheduledLesson != null) {
                String lessonRoomId = scheduledLesson.getRoomId();
                String lessonClassId = scheduledLesson.getClassId();

                if (!Objects.equals(lessonClassId, valueClassId)) {
                    if (lessonRoomId != null && Objects.equals(lessonRoomId, valueRoomId)) {
                        if (scheduledLesson.getScheduledTime().overlaps(valueTime)) {
                            conflictIds.add(lessonClassId);
                        }
                    }
                }
            }
        }

        return conflictIds;
    }

    @Override
    public void saveBest() {
        bestUnassignedVariableList = new ArrayList<>(unassignedVariableList);
        for(DefaultISGVariable var : variableList) {
            var.saveBest();
        }
    }

    @Override
    public void restoreBest() {
        unassignedVariableList = bestUnassignedVariableList;
        for(DefaultISGVariable var : variableList) {
            var.restoreBest();
        }
    }
}
