package thesis.solver.core;

import thesis.model.domain.elements.Timetable;

import java.util.List;

public interface ISGSolution<Model extends ISGModel, Variable extends ISGVariable> {
    Timetable solution();
    Model getModel(); //model

    void addUnassignedVariable(Variable var);
    List<Variable> getUnassignedVariables();
    List<Variable> getAssignedVariables();
    List<Variable> getBestUnassignedVariables();

    Boolean wasBestSaved();
    int getBestValue();
    int getTotalValue();

    boolean isSolutionValid();

    void convertToAssigned(Variable var);
    void convertToUnassigned(Variable var);

    //store and restore the best solution
    void saveBest();
    void restoreBest();
}
