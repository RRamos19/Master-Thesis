package thesis.service;

import thesis.model.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealing implements HeuristicAlgorithm<Timetable, DomainModel> {
    private final Timetable initialSolution;
    private final double initialTemperature;
    private final double coolingRate;
    private final double minTemperature;
    private final int k;
    private final Random random = new Random();
    private final List<neighborFindingMethod<Timetable>> neighborFunctions = List.of(this::moveClass, this::swapClasses);

    public SimulatedAnnealing(DomainModel data, InitialSolutionGenerator<Timetable, ClassUnit> initialSolutionGenerator, int maxInitialSolutionIterations, double initialTemperature, double minTemperature, double coolingRate, int k) {
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;

        List<ClassUnit> classesToSchedule = new ArrayList<>();
        // For a given course a random configuration is chosen. In said configuration one of each
        // class in a Subpart should be scheduled.
        for(Course course : data.getCourses()) {
            List<Config> configList = course.getConfigList();
            int configListSize = configList.size();

            // If there are no configs in this course it is skipped
            if(configListSize == 0) continue;

            // Choose a random config. From each subpart a random class is chosen
            Config chosenConfig = configList.get(random.nextInt(configListSize));
            for(Subpart subpart : chosenConfig.getSubpartList()) {
                List<ClassUnit> classUnitsList = subpart.getClassUnitList();
                int classUnitListSize = classUnitsList.size();

                // If there are no classes in this subpart it is skipped
                if(classUnitListSize == 0) continue;

                // A random class, of this subpart, is chosen to be scheduled
                classesToSchedule.add(classUnitsList.get(random.nextInt(classUnitListSize)));
            }
        }

        this.initialSolution = initialSolutionGenerator.generate(classesToSchedule, maxInitialSolutionIterations);
        this.initialSolution.setProgram(data.getProblemName());
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
                    double p = probabilityFunction(currentCost, fv, temperature);
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

    private int costFunction(Timetable solution) {
        int cost = 0;
        for(ScheduledLesson scheduledLesson : solution.getScheduledLessonList()) {
            //TODO: falta implementar
        }
        return cost;
    }

    private double coolingSchedule(int iter) {
        return initialTemperature * Math.exp(-coolingRate * iter);
    }

    private double probabilityFunction(double currentCost, double neighborCost, double temperature) {
        return currentCost != 0 ? Math.exp((currentCost - neighborCost) / (currentCost * temperature)) : 0;
    }

    private Timetable neighborhoodFunction(Timetable curr) {
        if(neighborFunctions.isEmpty()) {
            throw new IllegalStateException("There are no neighbor finding functions!");
        }

        int randomIndex = random.nextInt(neighborFunctions.size());
        return neighborFunctions.get(randomIndex).findNeighbor(curr);
    }

    private Timetable moveClass(Timetable solution) {
        Timetable neighbor = solution.clone();

        System.out.println("MoveClass!");

        return neighbor;
    }

    private Timetable swapClasses(Timetable solution) {
        Timetable neighbor = solution.clone();

        System.out.println("SwapClass!");

        return neighbor;
    }
}
