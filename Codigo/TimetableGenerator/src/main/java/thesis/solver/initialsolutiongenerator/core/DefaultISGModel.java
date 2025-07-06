package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.Constraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultISGModel implements ISGModel<DefaultISGValue, DefaultISGVariable, Constraint> {
    private List<DefaultISGVariable> variableList;
    private List<DefaultISGVariable> unassignedVariableList;
    private List<DefaultISGVariable> bestUnassignedVariableList = null;
    private DefaultISGSolution solution;

    @Override
    public List<DefaultISGVariable> variables() {
        return variableList;
    }

    @Override
    public void addVariable(DefaultISGVariable variable) {
        variableList.add(variable);
    }

    public DefaultISGSolution createInitialSolution() {
        solution = new DefaultISGSolution(this);
        variableList = solution.getVariableList();
        unassignedVariableList = solution.getUnassignedVariables();
        return solution;
    }

    public List<DefaultISGVariable> getBestUnassignedVariables() {
        return bestUnassignedVariableList;
    }

    @Override
    public void removeVariable(DefaultISGVariable variable) {
        // TODO: otimizar se necessário
        for(int i=0; i<variableList.size(); i++) {
            DefaultISGVariable var = variableList.get(i);
            if(variable.equals(var)) {
                variableList.remove(i);
                return;
            }
        }
    }

    @Override
    public Set<DefaultISGValue> conflictValues(DefaultISGValue value) {
        HashSet<DefaultISGValue> conflictValues = new HashSet<>();
        HashSet<String> conflictIds = new HashSet<>();

        if(value.variable() != null) {
            for (Constraint constraint : value.variable().variable().getConstraintList()) {
                constraint.computeConflicts(value.value(), solution.solution(), conflictIds);
            }

            for (String str : conflictIds) {
                // TODO: otimizar
                for (DefaultISGVariable var : variableList) {
                    String classId = var.variable().getClassId();

                    if (classId.equals(str)) {
                        conflictValues.add(var.getAssignment());
                        break;
                    }
                }
            }
        }

        return conflictValues;
    }

    @Override
    public void saveBest() {
        bestUnassignedVariableList = new ArrayList<>(unassignedVariableList);
        for(DefaultISGVariable var : variableList) {
            var.saveBest();
        }
    }

    @Override
    public void restoreBest() {
        unassignedVariableList = bestUnassignedVariableList;
        for(DefaultISGVariable var : variableList) {
            var.restoreBest();
        }
    }
}
