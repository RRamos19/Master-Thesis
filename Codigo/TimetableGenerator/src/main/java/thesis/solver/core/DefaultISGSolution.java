package thesis.solver.core;

import thesis.model.domain.Constraint;
import thesis.model.domain.Timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultISGSolution implements ISGSolution<Timetable, DefaultISGModel, DefaultISGVariable>, Cloneable {
    private List<DefaultISGVariable> variableList = new ArrayList<>();
    private List<DefaultISGVariable> unassignedVariableList = new ArrayList<>();
    private DefaultISGModel model;
    private Integer bestValue = null;
    private int UNASSIGNED_PENALTY = 1000;
//    private long iteration = 0;
//    private double time = 0;

    public DefaultISGSolution(DefaultISGModel model) {
        this.model = model;
    }

    @Override
    public Timetable solution() {
        Timetable timetable = new Timetable();

        for(DefaultISGVariable var : variableList) {
            if(var.getAssignment() != null) {
                timetable.addScheduledLesson(var.getAssignment().value());
            }
        }

        return timetable;
    }

    @Override
    public List<DefaultISGVariable> getUnassignedVariables() {
        return unassignedVariableList;
    }

    @Override
    public List<DefaultISGVariable> getAssignedVariables() {
        return variableList;
    }

//    //current iteration
//    @Override
//    public long getIteration() {
//        return iteration;
//    }
//
//    //current solution time
//    @Override
//    public double getTime() {
//        return time;
//    }

    @Override
    public DefaultISGModel getModel() {
        return model;
    }

    @Override
    public Boolean wasBestSaved() {
        return bestValue != null;
    }

    @Override
    public int getBestValue() {
        return bestValue;
    }

    @Override
    public int getTotalValue() {
        int total = 0;
        List<Constraint> constraintList = new ArrayList<>();

        // Sum the penalties of the time blocks and rooms
        for(DefaultISGVariable var : variableList) {
            DefaultISGValue value = var.getAssignment();
            if(value == null) {
                total += UNASSIGNED_PENALTY;
                continue;
            }

            total += value.toInt();
            constraintList.addAll(var.getConstraintList());
        }

        // Sum the penalties of the violated soft constraints
        Timetable currentSolution = solution();
        for(Constraint constraint : constraintList) {
            total += constraint.computePenalties(currentSolution);
        }

        return total;
    }

    @Override
    public void addUnassignedVariable(DefaultISGVariable var) {
        unassignedVariableList.add(var);
    }

    @Override
    public boolean isSolutionValid() {
        return unassignedVariableList.isEmpty();
    }

    @Override
    public void convertToAssigned(DefaultISGVariable var) {
        if(!unassignedVariableList.contains(var)) {
            // Should be impossible, unless there is a bug
            throw new RuntimeException("Unassigned variable was not found!");
        }

        unassignedVariableList.remove(var);
        variableList.add(var);
    }

    @Override
    public void convertToUnassigned(DefaultISGVariable var) {
        if(!variableList.contains(var)) {
            // Should be impossible, unless there is a bug
            throw new RuntimeException("Assigned variable was not found!");
        }

        variableList.remove(var);
        unassignedVariableList.add(var);
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
        return //iteration == that.iteration &&
                //Double.compare(time, that.time) == 0 &&
                Objects.equals(variableList, that.variableList) &&
                Objects.equals(unassignedVariableList, that.unassignedVariableList) &&
                Objects.equals(bestValue, that.bestValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableList, unassignedVariableList, model, bestValue);//, iteration, time);
    }

    @Override
    public DefaultISGSolution clone() {
        try {
            DefaultISGSolution clone = (DefaultISGSolution) super.clone();
            clone.variableList = new ArrayList<>(this.variableList);
            clone.unassignedVariableList = new ArrayList<>(this.unassignedVariableList);
            clone.model = new DefaultISGModel();
            clone.model.setSolution(clone);
            clone.bestValue = this.bestValue;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
