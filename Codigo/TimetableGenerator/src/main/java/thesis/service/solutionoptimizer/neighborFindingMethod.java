package thesis.service.solutionoptimizer;

@FunctionalInterface
public interface neighborFindingMethod<T> {
    public T findNeighbor(T solution);
}
