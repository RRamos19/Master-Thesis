package thesis.service;

import thesis.model.domain.DomainModel;
import thesis.model.persistence.entities.TimetableEntity;

public class SimulatedAnnealing implements HeuristicAlgorithm<TimetableEntity, DomainModel> {
    private final TimetableEntity initialSolution;
    private final double initialTemperature;
    private final double coolingRate;
    private final String program;
    private final double minTemperature;
    private final int k;

    public SimulatedAnnealing(DomainModel data, InitialSolutionGenerator<TimetableEntity, DomainModel> initialSolutionGenerator, int maxInitialSolutionIterations, double initialTemperature, double minTemperature, double coolingRate, int k) {
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;
        this.program = data.getProblemName();

        this.initialSolution = initialSolutionGenerator.generate(data, maxInitialSolutionIterations);
    }

    @Override
    public TimetableEntity execute() {
        TimetableEntity currentSolution = initialSolution;
        int currentCost = costFunction(currentSolution);

        TimetableEntity bestSolutionFound = currentSolution;
        int bestSolutionCost = currentCost;

        double temperature = initialTemperature;

        int iteration = 0;
        while(temperature > minTemperature) {
            for(int i=0; i < k; i++) {
                TimetableEntity neighbor = neighborhoodFunction(currentSolution);
                int fv = costFunction(neighbor);

                // Minimize the cost
                if(fv < currentCost) {
                    // If the cost of the neighbor is lower than the cost of the current solution
                    // it is accepted immediately
                    currentSolution = neighbor;
                    currentCost = fv;

                    // Update the best solution found
                    if(currentCost < bestSolutionCost) {
                        bestSolutionCost = currentCost;
                        bestSolutionFound = currentSolution;
                    }
                } else {
                    // The neighbor is worse than the current solutions
                    // so its acceptance is based on a probability
                    double p = probabilityFunction();
                    if(Math.random() <= p) {
                        currentSolution = neighbor;
                        currentCost = fv;
                    }
                }
            }

            iteration++;
            temperature = coolingSchedule(iteration);
        }

        return bestSolutionFound;
    }

    private int costFunction(TimetableEntity solution) {
        // TODO: falta implementar
        return 0;
    }

    private double coolingSchedule(int iter) {
        return initialTemperature * Math.exp(-coolingRate * iter);
    }

    private double probabilityFunction() {
        // TODO: falta implementar
        return 0;
    }

    private TimetableEntity neighborhoodFunction(TimetableEntity curr) {
        // TODO: falta implementar
        return null;
    }
}
