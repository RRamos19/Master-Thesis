package thesis.model.solver.solutionoptimizer;

@FunctionalInterface
public interface neighborFindingMethod<T> {
    T findNeighbor(T solution);
}
