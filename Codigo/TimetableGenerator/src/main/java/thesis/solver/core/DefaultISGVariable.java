package thesis.solver.core;

import thesis.model.domain.ClassUnit;
import thesis.model.domain.Constraint;

import java.util.*;

public class DefaultISGVariable implements ISGVariable<ClassUnit, DefaultISGValue, DefaultISGSolution> {
    private final ClassUnit classUnit;                  // Assigned ClassUnit. Value will be linked to said class
    private DefaultISGValue iAssignment = null;         // Assigned value
    private DefaultISGValue iBestAssignment = null;     // Best assignment value
    private DefaultISGSolution solution;                // Solution of which the variable belongs to
    private final Map<DefaultISGValue, Integer> removalCount = new HashMap<>();

    public DefaultISGVariable(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    @Override
    public DefaultISGValue getAssignment() {
        return iAssignment;
    }

    @Override
    public void setSolution(DefaultISGSolution solution) {
        this.solution = solution;
    }

    @Override
    public ClassUnit variable() {
        return classUnit;
    }

    @Override
    public int getRemovals(DefaultISGValue value) {
        return removalCount.getOrDefault(value, 0);
    }

    @Override
    public ISGValueList<DefaultISGValue> getValues() {
        return new ScheduledClassValueList(this);
    }

    public List<Constraint> getConstraintList() {
        return classUnit.getConstraintList();
    }

    @Override
    public void unassign() {
        removalCount.put(iAssignment, getRemovals(iAssignment) + 1);
        iAssignment = null;
        solution.convertToUnassigned(this);
    }

    @Override
    public void assign(DefaultISGValue value) {
        if (iAssignment != null) unassign();
        iAssignment = value;
        solution.convertToAssigned(this);

        Set<String> classConflicts = solution.getModel().conflictValues(value);

        // Unassign all the conflicts
        for(String classId : classConflicts) {
            // Usage of a snapshot of the variableList. The original will be modified by
            // the unassignment and this prevents a ConcurrentModificationException
            for(DefaultISGVariable var : new ArrayList<>(solution.getAssignedVariables())) {
                if(Objects.equals(var.classUnit.getClassId(), classId)) {
                    var.unassign();
                    break;
                }
            }
        }

    }

    @Override
    public void saveBest() {
        iBestAssignment = iAssignment;
    }

    @Override
    public void restoreBest() {
        iAssignment = iBestAssignment;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGVariable)) return false;
        DefaultISGVariable that = (DefaultISGVariable) o;
        return Objects.equals(classUnit, that.classUnit) &&
                Objects.equals(iAssignment, that.iAssignment) &&
                Objects.equals(iBestAssignment, that.iBestAssignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnit, iAssignment, iBestAssignment, solution);
    }

    @Override
    public DefaultISGSolution getSolution() {
        return solution;
    }
}
