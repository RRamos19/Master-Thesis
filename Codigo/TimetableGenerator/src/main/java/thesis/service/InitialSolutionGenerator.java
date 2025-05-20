package thesis.service;

public interface InitialSolutionGenerator<T, S> {
    /**
     * Generates an initial solution
     * @return The solution generated
     */
    T generate(S data, int maxIterations);
}
