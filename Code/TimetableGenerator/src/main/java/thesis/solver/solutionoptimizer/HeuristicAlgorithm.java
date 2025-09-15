package thesis.solver.solutionoptimizer;

public interface HeuristicAlgorithm<T> {
    void stopAlgorithm();
    double getProgress();
    T execute();
}
