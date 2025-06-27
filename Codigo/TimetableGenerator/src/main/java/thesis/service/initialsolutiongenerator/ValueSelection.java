package thesis.service.initialsolutiongenerator;

public interface ValueSelection <VL, S, VR> {
    public VL selectValue(S solution, VR selectedVariable);
}
