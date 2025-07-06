package thesis.solver.initialsolutiongenerator;

import thesis.model.domain.*;
import thesis.solver.initialsolutiongenerator.core.*;
import thesis.utils.RandomUtils;

import java.util.List;

/**
 * The solution generator presented is based on Tomáš Müller's phd thesis.
 * Source - Constraint Based Timetabling https://muller.unitime.org/phd-thesis.pdf
 */
public class MullerSolutionGenerator implements InitialSolutionGenerator<Timetable> {
    private boolean interruptAlgorithm = false;
    private final DefaultISGModel model = new DefaultISGModel();
    private final ValueSelection<DefaultISGValue, DefaultISGSolution, DefaultISGVariable> valueSelection = new DefaultValueSelection();
    private final DefaultISGSolutionComparator defaultISGSolutionComparator = new DefaultISGSolutionComparator();
    private final List<ClassUnit> unscheduled;


    public MullerSolutionGenerator(List<ClassUnit> unscheduled) {
        this.unscheduled = unscheduled;
    }


    public Timetable generate(int maxIterations) {
        DefaultISGSolution solution = model.createInitialSolution();

        for(ClassUnit cls : unscheduled) {
            solution.addUnassignedVariable(new DefaultISGVariable(cls));
        }

        for(DefaultISGVariable var : solution.getUnassignedVariables()) {
            var.setSolution(solution);
        }

        int iter = 0;
        while(!solution.getUnassignedVariables().isEmpty() && iter < maxIterations && !interruptAlgorithm) {
            iter++;
            DefaultISGVariable variable = selectVariable(solution);

            // Should be impossible because the list of unscheduled is based on the data in DomainModel
            assert(variable.variable() != null);

            DefaultISGValue value = valueSelection.selectValue(solution, variable);

            if(value != null) {
                variable.assign(value);
            } else {
                variable.unassign();
            }

            if(defaultISGSolutionComparator.isBetterThanBestSolution(solution)) {
                solution.saveBest();
            }
        }

        solution.restoreBest();

        return solution.solution();
    }


    /**
     * Choose a class at random. The list of unscheduled classes is prioritized if there are classes that still need to be scheduled. Otherwise, a random already scheduled class is chosen to be rescheduled.
     * @param solution Contains all of the data needed to create a solution.
     * @return Class id of the selected class
     */
    private DefaultISGVariable selectVariable(DefaultISGSolution solution) {
        List<DefaultISGVariable> unassignedVariables = solution.getUnassignedVariables();

        if(!unassignedVariables.isEmpty()) {
            return RandomUtils.random(unassignedVariables);
        } else {
            return RandomUtils.random(solution.getAssignedVariables());
        }
    }


    private void stopAlgorithm() {
        interruptAlgorithm = true;
    }
}
