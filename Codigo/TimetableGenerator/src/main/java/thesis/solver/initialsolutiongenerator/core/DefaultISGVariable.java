package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.ClassUnit;
import thesis.model.domain.Constraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DefaultISGVariable implements ISGVariable<ClassUnit, DefaultISGValue, Constraint> {
    private final ClassUnit classUnit; // Assigned ClassUnit. Value will be linked to said class
    private DefaultISGValue iAssignment = null; //assigned value
    private DefaultISGValue iInitialAssignment = null; //initial value (MPP)
    private DefaultISGValue iBestAssignment = null; //best assignment value
    private DefaultISGSolution solution;

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

    @Override
    public List<DefaultISGValue> getValues() {
        return new VirtualScheduledClassList(this);
    }

    @Override
    public void unassign() {
        iAssignment.unassign();
        iAssignment = null;
        solution.convertToUnassigned(this);

        System.out.println("Unassignment! Unassigned variables: " + solution.getUnassignedVariables().size());
    }

    @Override
    public void assign(DefaultISGValue value) {
        if (iAssignment != null) unassign();
        if (iInitialAssignment == null) iInitialAssignment = value;
        iAssignment = value;
        solution.convertToAssigned(this);
        iAssignment.assign(this);

        System.out.println("Assignment! Unassigned variables: " + solution.getUnassignedVariables().size());

        HashSet<String> classConflicts = new HashSet<>();
        for(Constraint constraint : classUnit.getConstraintList()) {
            constraint.computeConflicts(value.value(), solution.solution(), classConflicts);
        }

        for(String classId : classConflicts) {
            // Creation of a snapshot of the variableList. The original will be modified by
            // the unassignment and this prevents a ConcurrentModificationException
            for(DefaultISGVariable var : new ArrayList<>(solution.getVariableList())) {
                if(var.classUnit.getClassId().equals(classId)) {
                    var.unassign();
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
}
