package thesis.service;

@FunctionalInterface
public interface neighborFindingMethod<T> {
    public T findNeighbor(T solution);
}
