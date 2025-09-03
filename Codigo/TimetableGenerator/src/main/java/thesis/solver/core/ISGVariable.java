package thesis.solver.core;

public interface ISGVariable<Var extends ISGVariable<Var, Val, ?>, Val extends ISGValue<Val, Var>, Sol extends ISGSolution<?, Val, Var>> {
    Object variable();
    Val getAssignment();

    void assign(Val value);
    void unassign();

    int getRemovals(Val value);

    ISGValueList<Val> getValues();

    void setSolution(Sol solution);
    Sol getSolution();

    void saveBest();
    void restoreBest();
}
