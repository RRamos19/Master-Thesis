package thesis.solver.core;

import thesis.model.domain.InMemoryRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ISGSolution<modelRepository extends InMemoryRepository, Val extends ISGValue<Val, Var>, Var extends ISGVariable<Var, Val, ?>> {
    Object solution();

    void addUnassignedVariable(Var var);
    Collection<Var> getUnassignedVariables();
    Collection<Var> getAssignedVariables();
    Collection<Var> getBestUnassignedVariables();

    /**
     * Returns a Set of the violated hard restrictions.
     * @param value Value to be assigned.
     * @return Set of hard restrictions violated.
     */
    Collection<?> conflictIds(Val value);

    Boolean wasBestSaved();
    int getBestValue();
    int getTotalValue();

    void incrementIteration();
    long getIteration();

    void deactivateRemovals();

    modelRepository getDataModel();

    /**
     * Checks if the solution is complete.
     * @return True if all the unassigned variables were assigned. False otherwise.
     */
    boolean isSolutionValid();

    void convertToAssigned(Var var);
    void convertToUnassigned(Var var);

    // store and restore the best solution
    void saveBest();
    void restoreBest();
}
