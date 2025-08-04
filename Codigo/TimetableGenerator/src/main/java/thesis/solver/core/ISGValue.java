package thesis.solver.core;

public interface ISGValue<DomainValue, Variable extends ISGVariable> {
    DomainValue value();
    Variable variable();

    int getRemovals();

    boolean isAvailable();

    int toInt();
}
