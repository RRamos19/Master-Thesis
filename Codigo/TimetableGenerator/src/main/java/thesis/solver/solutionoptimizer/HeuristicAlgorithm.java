package thesis.solver.solutionoptimizer;

public interface HeuristicAlgorithm<T, S> {
    double getProgress();
    T execute();
}
