package thesis.solver.core;

public interface ISGVariable<Var extends ISGVariable<Var, Val, ?>, Val extends ISGValue<Val, Var>, Sol extends ISGSolution<?, Val, Var>> {
    /**
     * Returns the object this variable represents.
     * @return The variable object of this class.
     */
    Object variable();
    Val getAssignment();

    void assign(Val value);
    void unassign();

    int getRemovals(Val value);
    void deactivateRemovals();

    ISGValueList<Val> getValues();

    void setSolution(Sol solution);
    Sol getSolution();

    void saveBest();
    void restoreBest();
}
