package thesis.solver.initialsolutiongenerator.core;

public interface ValueSelection <VL, S, VR> {
    public VL selectValue(S solution, VR selectedVariable);
}
