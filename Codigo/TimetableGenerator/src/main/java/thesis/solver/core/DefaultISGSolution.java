package thesis.solver.core;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.Timetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DefaultISGSolution implements ISGSolution<DefaultISGModel, DefaultISGVariable>, Cloneable {
    private final List<DefaultISGVariable> variableList = Collections.synchronizedList(new ArrayList<>());
    private final List<DefaultISGVariable> unassignedVariableList = Collections.synchronizedList(new ArrayList<>());
    private List<DefaultISGVariable> bestUnassignedVariableList;
    private List<DefaultISGVariable> bestAssignedVariableList;
    private DefaultISGModel model;
    private Integer bestValue;

    public DefaultISGSolution(DefaultISGModel model) {
        this.model = model;
    }

    @Override
    public Timetable solution() {
        InMemoryRepository dataModel = model.getDataModel();

        Timetable timetable = new Timetable(dataModel.getProgramName());
        timetable.bindDataModel(dataModel);

        for(DefaultISGVariable var : variableList) {
            timetable.addScheduledLesson(var.getAssignment().value());
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

    @Override
    public List<DefaultISGVariable> getBestUnassignedVariables() {
        return bestUnassignedVariableList;
    }

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
        return solution().cost();
    }

    @Override
    public void addUnassignedVariable(DefaultISGVariable var) {
        unassignedVariableList.add(var);
    }

    @Override
    public boolean isSolutionValid() {
        if(!unassignedVariableList.isEmpty())
            return false;

        // Should not be needed but just for double checking
        for(DefaultISGVariable var : variableList) {
            if(var.getAssignment() == null)
                return false;
        }

        return true;
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

    @Override
    public void saveBest() {
        bestUnassignedVariableList = new ArrayList<>(unassignedVariableList);
        bestUnassignedVariableList.forEach(DefaultISGVariable::saveBest);

        bestAssignedVariableList = new ArrayList<>(variableList);
        bestAssignedVariableList.forEach(DefaultISGVariable::saveBest);

        bestValue = getTotalValue();
    }

    @Override
    public void restoreBest() {
        unassignedVariableList.clear();
        unassignedVariableList.addAll(bestUnassignedVariableList);
        unassignedVariableList.forEach(DefaultISGVariable::restoreBest);

        variableList.clear();
        variableList.addAll(bestAssignedVariableList);
        variableList.forEach(DefaultISGVariable::restoreBest);

        if(bestValue != getTotalValue()) {
            System.out.println("Yep, não funciona");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGSolution)) return false;
        DefaultISGSolution that = (DefaultISGSolution) o;
        return Objects.equals(variableList, that.variableList) &&
                Objects.equals(unassignedVariableList, that.unassignedVariableList) &&
                Objects.equals(bestValue, that.bestValue) &&
                Objects.equals(bestUnassignedVariableList, that.bestUnassignedVariableList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableList, unassignedVariableList, model, bestValue);
    }

    @Override
    public DefaultISGSolution clone() {
        try {
            DefaultISGSolution clone = (DefaultISGSolution) super.clone();
            clone.variableList.addAll(this.variableList);
            clone.variableList.forEach((v) -> v.setSolution(clone));
            clone.unassignedVariableList.addAll(this.unassignedVariableList);
            clone.unassignedVariableList.forEach((v) -> v.setSolution(clone));
            clone.bestUnassignedVariableList = new ArrayList<>(this.bestUnassignedVariableList);
            clone.bestAssignedVariableList = new ArrayList<>(this.bestAssignedVariableList);
            clone.model = new DefaultISGModel(model, clone);
            clone.bestValue = this.bestValue;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
