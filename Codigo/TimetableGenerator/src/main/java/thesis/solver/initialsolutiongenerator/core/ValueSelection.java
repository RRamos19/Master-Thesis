package thesis.solver.initialsolutiongenerator.core;

public interface ValueSelection <VL, S, VR> {
    VL selectValue(S solution, VR selectedVariable);
}
