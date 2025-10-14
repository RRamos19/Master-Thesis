package thesis.model.solver.core;

public interface ISGSolutionComparator<Solution extends ISGSolution> {
    boolean isBetterThanBestSolution(Solution solution);
}
