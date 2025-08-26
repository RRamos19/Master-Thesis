package thesis.solver.core;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.Constraint;
import thesis.model.domain.elements.ScheduledLesson;
import thesis.model.domain.elements.Time;
import thesis.model.domain.elements.Timetable;

import java.util.*;

public class DefaultISGModel implements ISGModel<DefaultISGValue, DefaultISGVariable, DefaultISGSolution> {
    private final InMemoryRepository dataModel;
    private List<DefaultISGVariable> variableList;
    private List<DefaultISGVariable> unassignedVariableList;
    private DefaultISGSolution solution;

    public DefaultISGModel(DefaultISGModel model, DefaultISGSolution solution) {
        this.dataModel = model.dataModel;
        this.variableList = model.variableList;
        this.unassignedVariableList = model.unassignedVariableList;
        this.solution = solution;
    }

    public DefaultISGModel(InMemoryRepository dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DefaultISGSolution createInitialSolution() {
        solution = new DefaultISGSolution(this);
        variableList = solution.getAssignedVariables();
        unassignedVariableList = solution.getUnassignedVariables();
        return solution;
    }

    @Override
    public void setSolution(DefaultISGSolution solution) {
        this.solution = solution;
        this.variableList = solution.getAssignedVariables();
        this.unassignedVariableList = solution.getUnassignedVariables();
    }

    @Override
    public InMemoryRepository getDataModel() {
        return dataModel;
    }

    @Override
    public Set<String> conflictValues(DefaultISGValue value) {
        HashSet<String> conflictIds = new HashSet<>();

        Timetable timetable = solution.solution();

        // Add the constraint conflicts
        for (Constraint constraint : value.variable().variable().getConstraintList()) {
            constraint.computeConflicts(value.value(), timetable, conflictIds);
        }

        // Variables to avoid multiple method calls in the for loop
        ScheduledLesson valueLesson = value.value();
        String valueClassId = valueLesson.getClassId();
        String valueRoomId = valueLesson.getRoomId();
        Time valueTime = valueLesson.getScheduledTime();

        // Add the room conflicts
        for (DefaultISGVariable variable : solution.getAssignedVariables()) {
            ScheduledLesson scheduledLesson = variable.getAssignment().value();

            if (scheduledLesson != null) {
                String lessonRoomId = scheduledLesson.getRoomId();
                String lessonClassId = scheduledLesson.getClassId();

                if (Objects.equals(lessonClassId, valueClassId)) continue;

                if (lessonRoomId != null && Objects.equals(lessonRoomId, valueRoomId)) {
                    if (scheduledLesson.getScheduledTime().overlaps(valueTime)) {
                        conflictIds.add(lessonClassId);
                    }
                }
            }
        }


        return conflictIds;
    }

    @Override
    public void saveBest() {
        for(DefaultISGVariable var : variableList) {
            var.saveBest();
        }
    }

    @Override
    public void restoreBest() {
        for(DefaultISGVariable var : variableList) {
            var.restoreBest();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGModel)) return false;
        DefaultISGModel that = (DefaultISGModel) o;
        return Objects.equals(dataModel, that.dataModel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dataModel);
    }
}
