package thesis.model.solver.initialsolutiongenerator;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.solver.core.*;
import thesis.utils.RandomToolkit;

import java.util.ArrayList;
import java.util.List;

/**
 * The solution generator presented is based on Tomáš Müller's phd thesis.
 * Source - Constraint Based Timetabling https://muller.unitime.org/phd-thesis.pdf
 */
public class MullerBasedSolutionGenerator implements InitialSolutionGenerator<DefaultISGSolution> {
    private final InMemoryRepository dataModel;
    private final ValueSelection<DefaultISGValue, DefaultISGSolution, DefaultISGVariable> valueSelection = new DefaultValueSelection();
    private final DefaultISGSolutionComparator defaultISGSolutionComparator = new DefaultISGSolutionComparator();
    private final List<ClassUnit> unscheduled;
    private volatile boolean interruptAlgorithm = false;
    private DefaultISGSolution solution;

    // Signals that the progress may be consulted
    boolean setupComplete = false;

    public MullerBasedSolutionGenerator(InMemoryRepository data) {
        // Every class in every subpart must be allocated in the timetable.
        this.unscheduled = new ArrayList<>(data.getClassUnits());

        this.dataModel = data;
    }

    public DefaultISGSolution generate() {
        solution = new DefaultISGSolution(dataModel);
        convertToVariables(solution, unscheduled);

        setupComplete = true;

        while(!solution.isSolutionValid()) {
            if(interruptAlgorithm) {
                return null;
            }

            DefaultISGVariable variable = selectVariable(solution);

            // Should be impossible because the list of unscheduled is based on the data in DataRepository
            if(variable.variable() == null) {
                throw new RuntimeException("Initial Solution Generator: No class unit stored in variable");
            }

            DefaultISGValue value = valueSelection.selectValue(solution, variable);

            if(value != null) {
                variable.assign(value);
            } else {
                variable.unassign();
            }

            if(defaultISGSolutionComparator.isBetterThanBestSolution(solution)) {
                solution.saveBest();
            }

            solution.incrementIteration();
        }

        return solution;
    }

    /**
     * Choose a class at random from the list of unscheduled classes
     * @param solution Contains all the data needed to create a solution.
     * @return Class to be scheduled
     */
    private DefaultISGVariable selectVariable(DefaultISGSolution solution) {
        DefaultISGVariable variable = RandomToolkit.random(solution.getUnassignedVariables());
        if(variable == null) {
            variable = RandomToolkit.random(solution.getAssignedVariables());
        }

        if(variable == null) {
            throw new IllegalStateException("selectVariable: selected variable was null!");
        }

        return variable;
    }

    private void convertToVariables(DefaultISGSolution solution, List<ClassUnit> classUnitList) {
        // For each unscheduled class a variable is made which represents the class
        // Each variable is then assigned a value which represents the Room, Time and Teachers combination
        for(ClassUnit cls : classUnitList) {
            DefaultISGVariable var = new DefaultISGVariable(cls);
            solution.addUnassignedVariable(var);
            var.setSolution(solution);
        }
    }

    @Override
    public double getProgress() {
        if(!setupComplete) {
            return 0;
        }

        return 1.0 - ((double) solution.getUnassignedVariables().size()) / unscheduled.size();
    }

    @Override
    public void stopAlgorithm() {
        interruptAlgorithm = true;
    }
}
