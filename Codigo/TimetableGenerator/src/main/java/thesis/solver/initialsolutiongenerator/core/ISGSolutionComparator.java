package thesis.solver.initialsolutiongenerator.core;

public interface ISGSolutionComparator<Solution extends ISGSolution> {
    boolean isBetterThanBestSolution(Solution solution);
}
