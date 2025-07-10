package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.Time;
import thesis.model.domain.Timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultISGSolution implements ISGSolution<DefaultISGModel> {
    private final List<DefaultISGVariable> variableList = new ArrayList<>();
    private final List<DefaultISGVariable> unassignedVariableList = new ArrayList<>();
    private final DefaultISGModel model;
    private Integer bestValue = null;
    private long iteration = 0;
    private double time = 0;

    public DefaultISGSolution(DefaultISGModel model) {
        this.model = model;
    }

    public Timetable solution() {
        Timetable timetable = new Timetable();

        for(DefaultISGVariable var : variableList) {
            if(var.getAssignment() != null) {
                timetable.addScheduledLesson(var.getAssignment().value());
            }
        }

        return timetable;
    }

    public List<DefaultISGVariable> getUnassignedVariables() {
        return unassignedVariableList;
    }

    public List<DefaultISGVariable> getAssignedVariables() {
        return variableList;
    }

    //current iteration
    @Override
    public long getIteration() {
        return iteration;
    }

    //current solution time
    @Override
    public double getTime() {
        return time;
    }

    @Override
    public DefaultISGModel getModel() {
        return model;
    }

    public Integer getBestInfo() {
        return bestValue;
    }

    public List<DefaultISGVariable> getVariableList() {
        return variableList;
    }

    public int getTotalValue() {
        int total = 0;
        for(DefaultISGVariable var : variableList) {
            total += var.getAssignment().toInt();
        }
        return total;
    }

    public int getBestValue() {
        return bestValue;
    }

    public void addUnassignedVariable(DefaultISGVariable variable) {
        unassignedVariableList.add(variable);
    }

    public void convertToAssigned(DefaultISGVariable variable) {
        if(!unassignedVariableList.contains(variable)) {
            // Should be impossible unless there is a bug
            throw new RuntimeException("Unassigned variable was not found!");
        }

        unassignedVariableList.remove(variable);
        variableList.add(variable);
    }

    public void convertToUnassigned(DefaultISGVariable variable) {
        if(!variableList.contains(variable)) {
            // Should be impossible unless there is a bug
            throw new RuntimeException("Assigned variable was not found!");
        }

        variableList.remove(variable);
        unassignedVariableList.add(variable);
    }

    //store and restore the best solution
    @Override
    public void saveBest() {
        model.saveBest();
        bestValue = getTotalValue();
    }

    @Override
    public void restoreBest() {
        model.restoreBest();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGSolution)) return false;
        DefaultISGSolution that = (DefaultISGSolution) o;
        return iteration == that.iteration &&
                Double.compare(time, that.time) == 0 &&
                Objects.equals(variableList, that.variableList) &&
                Objects.equals(unassignedVariableList, that.unassignedVariableList) &&
                Objects.equals(bestValue, that.bestValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableList, unassignedVariableList, model, bestValue, iteration, time);
    }

    public boolean isAvailable(DefaultISGValue defaultISGValue) {
        String defaultISGValueRoomId = defaultISGValue.value().getRoomId();
        Time defaultISGValueTime = defaultISGValue.value().getScheduledTime();

        for(DefaultISGVariable var : variableList) {
            DefaultISGValue value = var.getAssignment();

            // If the values are the same ignore
            if(Objects.equals(value, defaultISGValue))
                continue;

            String valueRoomId = value.value().getRoomId();
            Time valueTime = value.value().getScheduledTime();

            if(Objects.equals(valueRoomId, defaultISGValueRoomId)) {
                if(defaultISGValueTime.overlaps(valueTime)) {
                    return false;
                }
            }
        }

        return true;
    }
}
