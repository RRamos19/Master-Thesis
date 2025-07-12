package thesis.solver.initialsolutiongenerator.core;

public interface ISGVariable<DomainVariable, Value extends ISGValue, Solution extends ISGSolution> {
    DomainVariable variable();
    Value getAssignment();

    void assign(Value value);
    void unassign();
    int getRemovals(Value value);

    ISGVirtualValueList<Value> getValues();

    void setSolution(Solution solution);

    void saveBest();
    void restoreBest();
}
