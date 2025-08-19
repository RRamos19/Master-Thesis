package thesis.solver.initialsolutiongenerator;

public interface InitialSolutionGenerator<T> {
    double getProgress();
    /**
     * Generates an initial solution
     *
     * @return The solution generated
     */
    T generate(Integer maxIterations);
}
