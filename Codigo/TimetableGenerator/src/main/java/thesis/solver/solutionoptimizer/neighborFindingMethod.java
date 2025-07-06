package thesis.solver.solutionoptimizer;

@FunctionalInterface
public interface neighborFindingMethod<T> {
    public T findNeighbor(T solution);
}
