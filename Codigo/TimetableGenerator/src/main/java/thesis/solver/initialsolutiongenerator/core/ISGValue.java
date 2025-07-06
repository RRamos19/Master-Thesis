package thesis.solver.initialsolutiongenerator.core;

public interface ISGValue<Val, Var extends ISGVariable<?, ?, ?>> {
    Val value();
    Var variable();
    void assign(Var variable);
    void unassign();
    boolean valueEquals(ISGValue<Val, Var> value);
    int toInt();
}
