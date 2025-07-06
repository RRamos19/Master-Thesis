package thesis.solver.initialsolutiongenerator;

public interface InitialSolutionGenerator<T> {
    /**
     * Generates an initial solution
     *
     * @return The solution generated
     */
    T generate(int maxIterations);
}
