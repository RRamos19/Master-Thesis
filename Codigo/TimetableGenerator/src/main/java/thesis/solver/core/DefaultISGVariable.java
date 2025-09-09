package thesis.solver.core;

import thesis.model.domain.components.ClassUnit;

import java.util.*;

public class DefaultISGVariable implements ISGVariable<DefaultISGVariable, DefaultISGValue, DefaultISGSolution> {
    private final Map<DefaultISGValue, Integer> removalCount; // Counts the number of times a certain value has been unassigned
    private final ClassUnit classUnit;                        // Assigned ClassUnit. Value will be linked to said class
    private DefaultISGValue iAssignment;                      // Assigned value
    private DefaultISGValue iBestAssignment;                  // Best assignment value
    private DefaultISGSolution solution;                      // Solution of which the variable belongs to

    public DefaultISGVariable(ClassUnit classUnit, boolean useRemovalCount) {
        this.classUnit = classUnit;

        if(useRemovalCount) {
            this.removalCount = new HashMap<>();
        } else {
            this.removalCount = null;
        }
    }

    public DefaultISGVariable(DefaultISGSolution newSol, DefaultISGVariable other) {
        this.classUnit = other.classUnit;

        if(other.iAssignment != null) {
            this.iAssignment = new DefaultISGValue(this, other.iAssignment);
        }

        if(other.iBestAssignment != null) {
            this.iBestAssignment = new DefaultISGValue(this, other.iBestAssignment);
        }

        if(other.removalCount != null) {
            this.removalCount = new HashMap<>(other.removalCount);
        } else {
            this.removalCount = null;
        }

        this.solution = newSol;
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
        if(removalCount == null) {
            throw new IllegalStateException("Cant get removals as the removal count map is null (incorrect initialization of class)");
        }
        return removalCount.getOrDefault(value, 0);
    }

    @Override
    public ISGValueList<DefaultISGValue> getValues() {
        return new ScheduledClassValueList(solution.getDataModel(), this);
    }

    @Override
    public void unassign() {
        if(removalCount != null) {
            removalCount.put(iAssignment, getRemovals(iAssignment) + 1);
        }
        iAssignment = null;
        solution.convertToUnassigned(this);
    }

    @Override
    public void assign(DefaultISGValue value) {
        if (iAssignment != null) {
            if (iAssignment.equals(value)) return; // There is no need to assign the same value to this variable

            unassign();
        }
        iAssignment = value;
        solution.convertToAssigned(this);

        Set<String> classConflicts = solution.conflictIds(value);

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
    public DefaultISGSolution getSolution() {
        return solution;
    }

    public String toString() {
        return classUnit.getClassId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGVariable)) return false;
        DefaultISGVariable that = (DefaultISGVariable) o;
        return Objects.equals(classUnit.getClassId(), that.classUnit.getClassId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnit.getClassId());
    }
}
