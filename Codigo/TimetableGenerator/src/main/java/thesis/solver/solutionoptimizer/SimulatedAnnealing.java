package thesis.solver.solutionoptimizer;

import thesis.model.domain.*;
import thesis.utils.RandomToolkit;

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
            this::moveClass
//            this::swapClasses
    );

    public SimulatedAnnealing(Timetable initialSolution, double initialTemperature, double minTemperature, double coolingRate, int k) {
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;
        this.initialSolution = initialSolution;
    }

    @Override
    public Timetable execute() {
        Timetable currentSolution = initialSolution;
        int currentCost = costFunction(currentSolution);

        Timetable bestSolutionFound = currentSolution.clone();
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
        return solution.cost();
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

        return RandomToolkit.random(neighborFunctions).findNeighbor(curr);
    }

    private Timetable moveClass(Timetable solution) {
        Timetable neighbor = solution.clone();

        int nTries = 0;
        ScheduledLesson scheduledLesson;
        List<Time> possibleMoves = new ArrayList<>();
        do {
            scheduledLesson = RandomToolkit.random(neighbor.getScheduledLessonList());
            Time originalTime = scheduledLesson.getScheduledTime();
            if(originalTime == null) {
                System.out.println(scheduledLesson);
            }
            ClassUnit classUnit = scheduledLesson.getClassUnit();
            Room room = scheduledLesson.getRoom();
            List<Teacher> teachers = scheduledLesson.getTeachers();

            if(classUnit != null) {
                for (Time time : classUnit.getTimeSet()) {
                    // Only all the possible moves different from current time
                    if(time.equals(originalTime))
                        continue;

                    boolean timeUnavailable = false;
                    if(room != null) {
                        for (Time timeRoomUnavailabilities : room.getRoomUnavailabilities()) {
                            if(timeRoomUnavailabilities.overlaps(time)) {
                                timeUnavailable = true;
                                break;
                            }
                        }

                        if(timeUnavailable)
                            continue;
                    }

                    if(teachers != null) {
                        for(Teacher teacher : teachers) {
                            for (Time timeTeacherUnavailabilities : teacher.getTeacherUnavailabilities()) {
                                if(timeTeacherUnavailabilities.overlaps(time)) {
                                    timeUnavailable = true;
                                    break;
                                }
                            }
                        }

                        if(timeUnavailable)
                            continue;
                    }

                    if(solution.isTimeFree(time)) {
                        possibleMoves.add(time);
                    }
                }
            }
            nTries++;
        } while(possibleMoves.isEmpty() && nTries < 1000);

        // Assign a random possible time to the lesson
        if(!possibleMoves.isEmpty()) {
            scheduledLesson.setScheduledTime(RandomToolkit.random(possibleMoves));
        }

        return neighbor;
    }

    private Timetable swapClasses(Timetable solution) {
        Timetable neighbor = solution.clone();

        ScheduledLesson scheduledLesson;
        do {
            scheduledLesson = RandomToolkit.random(neighbor.getScheduledLessonList());
        } while(false);

        return neighbor;
    }
}
