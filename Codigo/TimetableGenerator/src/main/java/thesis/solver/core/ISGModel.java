package thesis.solver.core;

import thesis.model.domain.InMemoryRepository;

import java.util.Set;

public interface ISGModel<Value extends ISGValue, Variable extends ISGVariable, Solution extends ISGSolution> {
    void setSolution(Solution solution);

    Solution createInitialSolution();

    //constraints
    Set<?> conflictValues(Value value);

    InMemoryRepository getDataModel();

    void saveBest();
    void restoreBest();
}
