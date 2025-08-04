package thesis.solver.core;

import java.util.List;

public interface ISGSolution<DomainSolution, Model extends ISGModel, Variable extends ISGVariable> {
    DomainSolution solution();
//    long getIteration(); //current iteration
//    double getTime(); //current solution time
    Model getModel(); //model

    void addUnassignedVariable(Variable var);
    List<Variable> getUnassignedVariables();
    List<Variable> getAssignedVariables();

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
