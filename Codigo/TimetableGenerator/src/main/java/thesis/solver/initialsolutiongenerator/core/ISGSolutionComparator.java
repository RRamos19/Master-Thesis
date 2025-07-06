package thesis.solver.initialsolutiongenerator.core;

public interface ISGSolutionComparator<S> {
    boolean isBetterThanBestSolution(S solution);
}
