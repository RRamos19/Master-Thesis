package thesis.solver.initialsolutiongenerator.core;

import java.util.List;
import java.util.Set;

public interface ISGModel<Value extends ISGValue, Variable extends ISGVariable, Solution extends ISGSolution> {
    List<Variable> getBestUnassignedVariables();

    Solution createInitialSolution();

    //constraints
    Set<?> conflictValues(Value value);

    void saveBest();
    void restoreBest();
}
