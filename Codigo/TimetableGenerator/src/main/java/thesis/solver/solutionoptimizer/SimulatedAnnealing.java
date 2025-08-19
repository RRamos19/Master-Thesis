package thesis.solver.solutionoptimizer;

import thesis.model.domain.*;
import thesis.solver.core.*;
import thesis.utils.RandomToolkit;

import java.util.List;
import java.util.stream.Collectors;

public class SimulatedAnnealing implements HeuristicAlgorithm<Timetable, ClassUnit> {
    private final DefaultISGSolution initialSolution;
    private final double initialTemperature;
    private final double coolingRate;
    private final double minTemperature;
    private final int k;
    private final int maxIter;
    private int iter;

    // List of possible methods for neighbor finding
    private final List<neighborFindingMethod<DefaultISGSolution>> neighborFunctions = List.of(
            this::moveClass
//            this::swapClasses
    );

    public SimulatedAnnealing(Timetable initialSolution, double initialTemperature, double minTemperature, double coolingRate, int k) {
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;
        this.maxIter = (int) Math.floor(-Math.log(minTemperature / initialTemperature) / coolingRate);

        // Create the Objects required for the algorithm
        DefaultISGModel model = new DefaultISGModel();
        this.initialSolution = model.createInitialSolution();
        for(ScheduledLesson scheduledLesson : initialSolution.getScheduledLessonList()) {
            DefaultISGVariable variable = new DefaultISGVariable(scheduledLesson.getClassUnit());
            variable.setSolution(this.initialSolution);
            this.initialSolution.addUnassignedVariable(variable);
            variable.assign(new DefaultISGValue(variable, scheduledLesson));
        }
    }

    @Override
    public Timetable execute() {
        DefaultISGSolution currentSolution = initialSolution;
        int currentCost = costFunction(currentSolution);

        currentSolution.saveBest();
        int bestSolutionCost = currentCost;

        double temperature = initialTemperature;

        iter = 0;
        while(temperature > minTemperature) {
            for(int i=0; i < k; i++) {
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

            iter++;
            temperature = coolingSchedule(iter);
        }

        if(currentSolution.wasBestSaved()) {
            currentSolution.restoreBest();
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
        DefaultISGSolution neighbor = solution.clone();
        DefaultISGVariable selectedVar;

        // Always choose one of the unassigned variables first
        List<DefaultISGVariable> unassignedVars = neighbor.getUnassignedVariables();
        selectedVar = RandomToolkit.random(unassignedVars);

        // If there are no unassigned variables an assigned one is chosen
        if(selectedVar == null) {
            List<DefaultISGVariable> assignedVars = neighbor.getAssignedVariables();
            if (assignedVars.isEmpty()) return neighbor;

            selectedVar = RandomToolkit.random(assignedVars);
        }

        // Should be impossible if there are any variables present
        if(selectedVar == null) return neighbor;

        ISGValueList<DefaultISGValue> possibleValues = selectedVar.getValues();

        // Choose a different value of the current one (if possible)
        DefaultISGValue currentValue = selectedVar.getAssignment();
        List<DefaultISGValue> validValues = possibleValues.values().stream()
                .filter(val -> !val.equals(currentValue))
                .collect(Collectors.toList());

        DefaultISGValue newValue = RandomToolkit.random(validValues);

        // applies the mutation
        if (newValue != null) {
            selectedVar.assign(newValue);
        }

        return neighbor;
    }

    @Override
    public double getProgress() {
        return Math.min((double) iter / maxIter, 1);
    }

    private DefaultISGSolution swapClasses(DefaultISGSolution solution) {
        DefaultISGSolution neighbor = solution.clone();

        //ScheduledLesson scheduledLesson = RandomToolkit.random(neighbor.getScheduledLessonList());

        return neighbor;
    }
}
