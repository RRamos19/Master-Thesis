package thesis.solver.core;

public interface ISGValue<V extends ISGValue<V, Var>, Var extends ISGVariable<Var, V, ?>> {
    /**
     * Returns the object this class represents.
     * @return The value object of this class.
     */
    Object value();

    /**
     * Returns the variable that is assigned with this value.
     * @return The variable.
     */
    Var variable();

    /**
     * Returns the number of times this value has been removed from the solution.
     * @return The number of removals from the solution.
     */
    int getRemovals();

    /**
     * Returns the penalties of a combination that is associated to a Variable.
     * @return The sum of the penalties.
     */
    int toInt();
}
