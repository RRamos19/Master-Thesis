package thesis.solver.solutionoptimizer;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.ScheduledLesson;
import thesis.model.domain.elements.Timetable;
import thesis.solver.core.*;
import thesis.utils.RandomToolkit;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SimulatedAnnealing implements HeuristicAlgorithm<Timetable> {
    private final DefaultISGSolution initialSolution;
    private final double initialTemperature;
    private final double coolingRate;
    private final double minTemperature;
    private final int k;
    private final int maxIter;
    private AtomicInteger iter;
    private volatile boolean interruptAlgorithm = false;

    // List of possible methods for neighbor finding
    private final List<neighborFindingMethod<DefaultISGSolution>> neighborFunctions = List.of(
            this::moveClass
//            this::swapClasses
    );

    public SimulatedAnnealing(InMemoryRepository data, Timetable initialSolution, double initialTemperature, double minTemperature, double coolingRate, int k) {
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;
        this.maxIter = (int) Math.floor(-Math.log(minTemperature / initialTemperature) / coolingRate);

        // Create the Objects required for the algorithm
        DefaultISGModel model = new DefaultISGModel(data);
        this.initialSolution = model.createInitialSolution();
        for(ScheduledLesson scheduledLesson : initialSolution.getScheduledLessonList()) {
            DefaultISGVariable variable = new DefaultISGVariable(data, scheduledLesson.getClassUnit());
            variable.setSolution(this.initialSolution);
            this.initialSolution.addUnassignedVariable(variable);
            variable.assign(new DefaultISGValue(variable, scheduledLesson));
        }
    }

    @Override
    public Timetable execute() {
        DefaultISGSolution currentSolution = initialSolution;

        if(!currentSolution.isSolutionValid()) {
            throw new IllegalStateException("The initial solution must be valid");
        }

        int currentCost = costFunction(currentSolution);

        int bestSolutionCost = currentCost;
        currentSolution.saveBest();

        double temperature = initialTemperature;

        iter = new AtomicInteger(0);
        while(temperature > minTemperature && !interruptAlgorithm) {
            for(int i=0; i < k; i++) {

                if(interruptAlgorithm) {
                    break;
                }

                DefaultISGSolution neighbor = neighborhoodFunction(currentSolution);
                int fv = costFunction(neighbor);

                // Minimize the cost
                if(fv < currentCost) {
                    // If the cost of the neighbor is lower than the cost of the current solution
                    // it is accepted immediately
                    currentSolution = neighbor;
                    currentCost = fv;

                    // Update the best solution found if the solution is valid
                    if(currentCost < bestSolutionCost && currentSolution.isSolutionValid()) {
                        bestSolutionCost = currentCost;
                        currentSolution.saveBest();
                    }
                } else {
                    // The neighbor is worse than the current solutions
                    // so its acceptance is based on a probability
                    double p = probabilityFunction(currentCost, fv, temperature);
                    if(RandomToolkit.random() <= p) {
                        currentSolution = neighbor;
                        currentCost = fv;
                    }
                }
            }

            iter.incrementAndGet();
            temperature = coolingSchedule(iter.get());
        }

        if(currentSolution.wasBestSaved()) {
            currentSolution.restoreBest();
        }

        if(!currentSolution.isSolutionValid()) {
            throw new IllegalStateException("The solution is not valid after the optimization");
        }

        return currentSolution.solution();
    }

    private int costFunction(DefaultISGSolution solution) {
        return solution.getTotalValue();
    }

    private double coolingSchedule(int iter) {
        return initialTemperature * Math.exp(-coolingRate * iter);
    }

    private double probabilityFunction(double currentCost, double neighborCost, double temperature) {
        return currentCost != 0 ? Math.exp((currentCost - neighborCost) / (currentCost * temperature)) : 0;
    }

    private DefaultISGSolution neighborhoodFunction(DefaultISGSolution curr) {
        if(neighborFunctions.isEmpty()) {
            throw new IllegalStateException("There aren't any neighbor finding functions!");
        }

        return RandomToolkit.random(neighborFunctions).findNeighbor(curr);
    }

    private DefaultISGSolution moveClass(DefaultISGSolution solution) {
        DefaultISGVariable selectedVar;

        // Always choose one of the unassigned variables first
        List<DefaultISGVariable> unassignedVars = solution.getUnassignedVariables();
        selectedVar = RandomToolkit.random(unassignedVars);

        // If there are no unassigned variables an assigned one is chosen
        if(selectedVar == null) {
            List<DefaultISGVariable> assignedVars = solution.getAssignedVariables();
            if (assignedVars.isEmpty()) return solution;

            selectedVar = RandomToolkit.random(assignedVars);
        }

        // Should be impossible if there are any variables present
        if(selectedVar == null) return solution;

        ISGValueList<DefaultISGValue> possibleValues = selectedVar.getValues();

        // Choose a different value of the current one (if possible)
        DefaultISGValue currentValue = selectedVar.getAssignment();
        List<DefaultISGValue> validValues = possibleValues.values().stream()
                .filter(val -> !val.equals(currentValue))
                .collect(Collectors.toList());

        if(validValues.isEmpty()) return solution;

        DefaultISGValue newValue = RandomToolkit.random(validValues);

        // applies the mutation
        if (newValue != null) {
            selectedVar.assign(newValue);
        }

        return solution;
    }

    @Override
    public double getProgress() {
        if(iter == null) return 0;

        if(interruptAlgorithm) return 100;

        return Math.min((double) iter.get() / maxIter, 1);
    }

    @Override
    public void stopAlgorithm() {
        interruptAlgorithm = true;
    }

    private DefaultISGSolution swapClasses(DefaultISGSolution solution) {
        //ScheduledLesson scheduledLesson = RandomToolkit.random(neighbor.getScheduledLessonList());

        return solution;
    }
}
