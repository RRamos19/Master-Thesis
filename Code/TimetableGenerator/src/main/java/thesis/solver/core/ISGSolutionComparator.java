package thesis.solver.core;

public interface ISGSolutionComparator<Solution extends ISGSolution> {
    boolean isBetterThanBestSolution(Solution solution);
}
