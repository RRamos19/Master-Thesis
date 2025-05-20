package thesis.service;

import thesis.model.aggregates.StructuredTimetableData;
import thesis.model.entities.Timetable;

public class SimulatedAnnealing implements HeuristicAlgorithm<Timetable, StructuredTimetableData> {
    private Timetable initialSolution;
    private final double initialTemperature;
    private double coolingRate;
    private final String courseId;
    private final double minTemperature;
    private final int k;

    public SimulatedAnnealing(StructuredTimetableData data, InitialSolutionGenerator<Timetable, StructuredTimetableData> initialSolutionGenerator, String courseId, int maxInitialSolutionIterations, double initialTemperature, double minTemperature, double coolingRate, int k) {
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;
        this.courseId = courseId;

        this.initialSolution = initialSolutionGenerator.generate(data, maxInitialSolutionIterations);
    }

    @Override
    public Timetable execute() {
        Timetable currentSolution = initialSolution;
        int currentCost = costFunction(currentSolution);

        Timetable bestSolutionFound = currentSolution;
        int bestSolutionCost = currentCost;

        double temperature = initialTemperature;

        int iteration = 0;
        while(temperature > minTemperature) {
            for(int i=0; i < k; i++) {
                Timetable neighbor = neighborhoodFunction(currentSolution);
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

        // TODO: falta implementar
        return bestSolutionFound;
    }

    private int costFunction(Timetable solution) {
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

    private Timetable neighborhoodFunction(Timetable curr) {
        // TODO: falta implementar
        return null;
    }
}
