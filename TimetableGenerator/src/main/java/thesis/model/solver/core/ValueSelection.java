package thesis.model.solver.core;

public interface ValueSelection <VL, S, VR> {
    VL selectValue(S solution, VR selectedVariable);
}
