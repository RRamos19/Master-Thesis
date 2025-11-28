package thesis.model.solver.solutionoptimizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.model.domain.components.PenaltySum;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.solver.core.DefaultISGSolution;
import thesis.model.solver.core.DefaultISGValue;
import thesis.model.solver.core.DefaultISGVariable;
import thesis.model.solver.core.ISGValueList;
import thesis.utils.RandomToolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulatedAnnealing implements HeuristicAlgorithm<Timetable> {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedAnnealing.class);

    private static final double RANDOM_MUTATION_PROBABILITY = 0.15;
    private final static int MAX_TRIES = 5;
    private final DefaultISGSolution initialSolution;
    private final double initialTemperature;
    private final double coolingRate;
    private final double minTemperature;
    private final int k;
    private final int maxIter;
    private AtomicInteger iter;
    private volatile boolean interruptAlgorithm = false;

    private enum MutationType {
        ROOM, TIME, BOTH
    }

    // List of possible methods for neighbor finding
    private final List<neighborFindingMethod<DefaultISGSolution>> neighborFunctions = List.of(
            this::moveClass
    );

    public SimulatedAnnealing(DefaultISGSolution initialSolution, double initialTemperature, double minTemperature, double coolingRate, int k) {
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;
        this.maxIter = (int) Math.floor(-Math.log(minTemperature / initialTemperature) / coolingRate);
        this.initialSolution = initialSolution;
    }

    @Override
    public Timetable execute() {
        DefaultISGSolution currentSolution = new DefaultISGSolution(initialSolution);

        // This feature should only be used in the generation of the initial solution
        currentSolution.deactivateRemovals();

        if(!currentSolution.isSolutionValid()) {
            throw new IllegalStateException("The initial solution must be valid");
        }

        int currentCost = costFunction(currentSolution);

        int bestSolutionCost = currentCost;
        currentSolution.saveBest();

        double temperature = initialTemperature;

        iter = new AtomicInteger(0);
        while(temperature > minTemperature) {
            for(int i=0; i < k; i++) {
                if(interruptAlgorithm) {
                    return null;
                }

                DefaultISGSolution neighbor = neighborhoodFunction(currentSolution);
                int fv = costFunction(neighbor);

                // Minimize the cost
                if(fv < currentCost) {
                    // If the cost of the neighbor is lower than the cost of the current solution
                    // it is accepted immediately
                    currentSolution = neighbor;
                    currentCost = fv;

                    // Update the best solution found if the current cost is better than the best
                    if(currentCost < bestSolutionCost) {
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
        return Math.exp((currentCost - neighborCost) / temperature);
    }

    private DefaultISGSolution neighborhoodFunction(DefaultISGSolution curr) {
        DefaultISGSolution neighbor = new DefaultISGSolution(curr);

        if(neighborFunctions.isEmpty()) {
            throw new IllegalStateException("There aren't any neighbor finding functions!");
        }

        return RandomToolkit.random(neighborFunctions).findNeighbor(neighbor);
    }

    private MutationType getRandomChangeType(DefaultISGSolution solution) {
        // Calculate the probability of only changing the time, room or both
        PenaltySum solutionCost = solution.solution().cost();

        // Only considering time and room to calculate the probabilities correctly
        int totalValue = solution.getTotalValue() - solutionCost.getCommonPenalty();
        double changeRoomProb = (double) solutionCost.getRoomPenalty() / totalValue * (1 - RANDOM_MUTATION_PROBABILITY);

        double random = RandomToolkit.random();
        if(random <= RANDOM_MUTATION_PROBABILITY) {
            return MutationType.BOTH;
        } else if(random <= (RANDOM_MUTATION_PROBABILITY + changeRoomProb)) {
            return MutationType.ROOM;
        } else {
            return MutationType.TIME;
        }
    }

    /**
     * Move an assigned class to another time block, room or both.
     * The neighbors generated should always be possible and complete timetables (no unassigned variables and no hard penalties violated)
     * @param solution A copy of the current solution
     * @return A neighbor of the current solution
     */
    private DefaultISGSolution moveClass(DefaultISGSolution solution) {
        MutationType mutationType = getRandomChangeType(solution);

        DefaultISGVariable selectedVar;
        DefaultISGValue newValue;
        int n = 0;
        do {
            selectedVar = RandomToolkit.random(solution.getAssignedVariables());

            if (selectedVar == null) {
                throw new IllegalStateException("The assigned variables list of the solution is empty");
            }

            DefaultISGValue currentValue = selectedVar.getAssignment();
            ScheduledLesson currentLesson = currentValue.value();

            ISGValueList<DefaultISGValue> values = selectedVar.getValues();

            // List of values of which there are no conflicts
            List<DefaultISGValue> noConflictValues = new ArrayList<>();
            for (DefaultISGValue value : values) {
                if (value.equals(currentValue)) continue;

                ScheduledLesson valueLesson = value.value();

                switch(mutationType) {
                    case TIME:
                        // Ignore changes in room
                        if(!Objects.equals(valueLesson.getRoomId(), currentLesson.getRoomId())) continue;
                        break;
                    case ROOM:
                        // Ignore changes in time
                        if(!Objects.equals(valueLesson.getScheduledTime(), currentLesson.getScheduledTime())) continue;
                        break;
                }

                // Check if there are conflicts. If there aren't any, the value is stored in the list
                if (solution.conflictIds(value).isEmpty()) {
                    noConflictValues.add(value);
                }
            }

            newValue = RandomToolkit.random(noConflictValues);

            n++;

            // If the value is null it indicates there are no possible moves for this variable.
            // The process is repeated until there is a variable or the number of tries exceeded the MAX_TRIES
        } while(newValue == null && n < MAX_TRIES);

        // Apply the mutation if the value exists
        if(newValue != null) {
            selectedVar.assign(newValue);
        }

        return solution;
    }

    @Override
    public double getProgress() {
        if(iter == null) return 0;

        return Math.min((double) iter.get() / maxIter, 1);
    }

    @Override
    public void stopAlgorithm() {
        logger.info("Optimizing algorithm cancelled!");
        interruptAlgorithm = true;
    }
}
