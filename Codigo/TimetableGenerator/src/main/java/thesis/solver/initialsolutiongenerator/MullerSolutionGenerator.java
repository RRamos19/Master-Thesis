package thesis.solver.initialsolutiongenerator;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.solver.core.*;
import thesis.utils.RandomToolkit;

import java.util.ArrayList;
import java.util.List;

/**
 * The solution generator presented is based on Tomáš Müller's phd thesis.
 * Source - Constraint Based Timetabling https://muller.unitime.org/phd-thesis.pdf
 */
public class MullerSolutionGenerator implements InitialSolutionGenerator<Timetable> {
    private final InMemoryRepository dataModel;
    private final ValueSelection<DefaultISGValue, DefaultISGSolution, DefaultISGVariable> valueSelection = new DefaultValueSelection();
    private final DefaultISGSolutionComparator defaultISGSolutionComparator = new DefaultISGSolutionComparator();
    private final List<ClassUnit> unscheduled;
    private volatile boolean interruptAlgorithm = false;
    private DefaultISGSolution solution;

    public MullerSolutionGenerator(InMemoryRepository data) {
        this.unscheduled = new ArrayList<>();

        // Every class in every subpart must be allocated in the timetable. As such, every course
        // config and subpart needs to be searched.
        for(Course course : data.getCourses()) {
            for(Config config : course.getConfigList()) {
                for (Subpart subpart : config.getSubpartList()) {
                    this.unscheduled.addAll(subpart.getClassUnitList());
                }
            }
        }

        this.dataModel = data;
    }

    public Timetable generate(Integer maxIterations) {
        solution = new DefaultISGSolution(dataModel);

        // For each unscheduled class a variable is made which represents the class
        // Each variable is then assigned a value which represents the Room, Time and Teachers combination
        for(ClassUnit cls : unscheduled) {
            DefaultISGVariable var = new DefaultISGVariable(cls, true);
            solution.addUnassignedVariable(var);
            var.setSolution(solution);
        }

        while(!solution.isSolutionValid()) {
            if(interruptAlgorithm) {
                return null;
            }
            if(maxIterations != null && solution.getIteration() >= maxIterations) {
                break;
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

        if(solution.wasBestSaved()) {
            solution.restoreBest();
        }

        return solution.solution();
    }

    /**
     * Choose a class at random from the list of unscheduled classes
     * @param solution Contains all of the data needed to create a solution.
     * @return Class to be scheduled
     */
    private DefaultISGVariable selectVariable(DefaultISGSolution solution) {
        return RandomToolkit.random(solution.getUnassignedVariables());
    }

    @Override
    public double getProgress() {
        return 1.0 - ((double) solution.getUnassignedVariables().size()) / unscheduled.size();
    }

    @Override
    public void stopAlgorithm() {
        interruptAlgorithm = true;
    }
}
