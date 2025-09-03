package thesis.solver.initialsolutiongenerator;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.*;
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

        int iter = 0;
        while(!solution.isSolutionValid() && !interruptAlgorithm) {
            if(maxIterations != null && iter >= maxIterations) {
                break;
            }
            iter++;
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
            return RandomToolkit.random(unassignedVariables);
        } else {
            return RandomToolkit.random(solution.getAssignedVariables());
        }
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
