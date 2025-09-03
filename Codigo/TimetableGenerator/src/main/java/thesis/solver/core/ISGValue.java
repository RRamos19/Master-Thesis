package thesis.solver.core;

public interface ISGValue<V extends ISGValue<V, Var>, Var extends ISGVariable<Var, V, ?>> {
    Object value();
    Var variable();

    int getRemovals();

    int toInt();
}
