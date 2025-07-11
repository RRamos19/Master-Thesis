package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.ClassUnit;
import thesis.model.domain.Constraint;

import java.util.*;

public class DefaultISGVariable implements ISGVariable<ClassUnit, DefaultISGValue, Constraint> {
    private final ClassUnit classUnit;                  // Assigned ClassUnit. Value will be linked to said class
    private DefaultISGValue iAssignment = null;         // Assigned value
    private DefaultISGValue iBestAssignment = null;     // Best assignment value
    private DefaultISGSolution solution;                // Solution of which the variable belongs to

    private Map<DefaultISGValue, Integer> removalCount = new HashMap<>();

    public DefaultISGVariable(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public DefaultISGValue getAssignment() {
        return iAssignment;
    }

    public void setSolution(DefaultISGSolution solution) {
        this.solution = solution;
    }

    @Override
    public ClassUnit variable() {
        return classUnit;
    }

    public int getRemovals(DefaultISGValue value) {
        return removalCount.getOrDefault(value, 0);
    }

    @Override
    public ISGVirtualValueList<DefaultISGValue> getValues() {
        return new VirtualScheduledClassList(this);
    }

    @Override
    public void unassign() {
        int rCount = getRemovals(iAssignment);
        removalCount.put(iAssignment, ++rCount);
        iAssignment = null;
        solution.convertToUnassigned(this);

        // TODO: Debug remover quando deixar de ser necessário
        // System.out.println("Class " + classUnit.getClassId() + " Unassignment! Unassigned variables: " + solution.getUnassignedVariables().size());
    }

    @Override
    public void assign(DefaultISGValue value) {
        if (iAssignment != null) unassign();
        iAssignment = value;
        solution.convertToAssigned(this);

        // TODO: Debug remover quando deixar de ser necessário
        // System.out.println("Class " + classUnit.getClassId() + " Assignment! Unassigned variables: " + solution.getUnassignedVariables().size());

        Set<String> classConflicts = solution.getModel().conflictValues(value);

        // Unassign all the conflicts
        for(String classId : classConflicts) {
            // Usage of a snapshot of the variableList. The original will be modified by
            // the unassignment and this prevents a ConcurrentModificationException
            for(DefaultISGVariable var : new ArrayList<>(solution.getVariableList())) {
                if(Objects.equals(var.classUnit.getClassId(), classId)) {
                    var.unassign();
                    break;
                }
            }
        }

    }

    public void saveBest() {
        iBestAssignment = iAssignment;
    }

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

    public DefaultISGSolution getSolution() {
        return solution;
    }
}
