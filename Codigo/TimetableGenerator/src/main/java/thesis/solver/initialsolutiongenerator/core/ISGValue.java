package thesis.solver.initialsolutiongenerator.core;

public interface ISGValue<Val, Var extends ISGVariable<?, ?, ?>> {
    Val value();
    Var variable();
    int toInt();
}
