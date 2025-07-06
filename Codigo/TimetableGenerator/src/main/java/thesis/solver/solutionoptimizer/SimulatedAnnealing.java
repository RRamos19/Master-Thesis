package thesis.solver.solutionoptimizer;

import thesis.model.domain.*;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class SimulatedAnnealing implements HeuristicAlgorithm<Timetable, ClassUnit> {
    private final Timetable initialSolution;
    private final double initialTemperature;
    private final double coolingRate;
    private final double minTemperature;
    private final int k;

    // List of possible methods for neighbor finding
    private final List<neighborFindingMethod<Timetable>> neighborFunctions = List.of(
            this::moveClass,
            this::swapClasses
    );

    public SimulatedAnnealing(DomainModel data, int maxInitialSolutionIterations, double initialTemperature, double minTemperature, double coolingRate, int k) {
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;

        List<ClassUnit> classesToSchedule = new ArrayList<>();
        // For a given course a random configuration is chosen. In said configuration one of each
        // class in a Subpart should be scheduled.
        for(Course course : data.getCourses()) {
            List<Config> configList = course.getConfigList();

            // If there are no configs in this course it is skipped
            if(configList.isEmpty()) continue;

            // Choose a random config. From each subpart a random class is chosen
            Config chosenConfig = RandomUtils.random(configList);
            for(Subpart subpart : chosenConfig.getSubpartList()) {
                List<ClassUnit> classUnitList = subpart.getClassUnitList();

                // If there are no classes in this subpart it is skipped
                if(classUnitList.isEmpty()) continue;

                // A random class, of this subpart, is chosen to be scheduled
                classesToSchedule.add(RandomUtils.random(classUnitList));
            }
        }

        InitialSolutionGenerator<Timetable> initialSolutionGen = new MullerSolutionGenerator(classesToSchedule);

        long startTime = System.currentTimeMillis();
        this.initialSolution = initialSolutionGen.generate(maxInitialSolutionIterations);
        //this.initialSolution.printTimetable();
        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);
        System.out.println("Initial solution generation: " + duration/1000 + "s");

        // TODO: temporário remover depois de avaliar os resultados
        data.addTimetable(initialSolution);
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
                        bestSolutionFound = currentSolution.clone();
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
            //TODO: falta confirmar se basta isto
            cost += scheduledLesson.toInt();
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

        return RandomUtils.random(neighborFunctions).findNeighbor(curr);
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
