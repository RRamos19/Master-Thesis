package thesis.solver.solutionoptimizer;

@FunctionalInterface
public interface neighborFindingMethod<T> {
    T findNeighbor(T solution);
}
