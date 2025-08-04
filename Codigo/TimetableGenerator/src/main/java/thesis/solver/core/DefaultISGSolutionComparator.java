package thesis.solver.core;

public class DefaultISGSolutionComparator implements ISGSolutionComparator<DefaultISGSolution> {
    @Override
    public boolean isBetterThanBestSolution(DefaultISGSolution solution) {
        if (!solution.wasBestSaved()) {
            //there is no best solution yet saved
            return true;
        }

        int currentUnassigned = solution.getUnassignedVariables().size();
        int bestUnassigned = solution.getModel().getBestUnassignedVariables().size();

        if (bestUnassigned != currentUnassigned) {
            return bestUnassigned > currentUnassigned;
        }

        int currentValue = solution.getTotalValue();
        int bestValue = solution.getBestValue();

        return currentValue < bestValue;
    }
}
