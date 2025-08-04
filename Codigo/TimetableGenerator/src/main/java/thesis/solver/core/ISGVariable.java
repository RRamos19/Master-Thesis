package thesis.solver.core;

public interface ISGVariable<DomainVariable, Value extends ISGValue, Solution extends ISGSolution> {
    DomainVariable variable();
    Value getAssignment();

    void assign(Value value);
    void unassign();
    int getRemovals(Value value);

    ISGValueList<Value> getValues();

    void setSolution(Solution solution);
    Solution getSolution();

    void saveBest();
    void restoreBest();
}
