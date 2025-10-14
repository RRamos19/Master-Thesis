package thesis.model.solver.core;

import thesis.utils.DoubleToolkit;
import thesis.utils.RandomToolkit;

import java.util.*;

public class DefaultValueSelection implements ValueSelection<DefaultISGValue, DefaultISGSolution, DefaultISGVariable> {
    private final static float iRandomWalkProb = 0.02F;                          // random walk selection
    private final static float iWeightCoflicts = 100.0F;                         // weight of a conflict
    private final static float iWeightValue = 0.0F;                              // weight of a value (value.toInt())

    private final static float iWeightWeightedCoflicts = 1.0F;                   // CBS: CBS weighted conflict weight

    private final Map<DefaultISGValue, Long> tabuList = new HashMap<>();
    private final static int TABU_DURATION = 7;

    @Override
    public DefaultISGValue selectValue(DefaultISGSolution solution, DefaultISGVariable selectedVariable) {
        ISGValueList<DefaultISGValue> values = selectedVariable.getValues();

        if(RandomToolkit.random() <= iRandomWalkProb) {
            //random-walk
            return values.random();
        }

        // values with the lowest weighted sum
        List<DefaultISGValue> bestValues = null;
        double bestWeightedSum = Double.MAX_VALUE;

        // go through all the values (the ISGValueList creates the objects on demand)
        for(DefaultISGValue value : values) {
            if(Objects.equals(value, selectedVariable.getAssignment())) {
                // do not pick the same value as it is currently assigned
                // if there is a value assigned to the selected variable
                continue;
            }

            Long tabuUntilIteration = tabuList.get(value);
            // Value is found on the tabu list
            if(tabuUntilIteration != null && tabuUntilIteration > solution.getIteration()) {
                continue;
            }

            Collection<String> conf = solution.conflictIds(value);

            int weightedConflicts = value.getRemovals(); // CBS weighted conflicts

            // Weighted sum of several criteria
            double weightedSum = (iWeightWeightedCoflicts * weightedConflicts) +
                        (iWeightCoflicts * conf.size()) +
                        (iWeightValue * value.toInt());

            // Store best values
            if(bestValues == null || bestWeightedSum > weightedSum) {
                bestWeightedSum = weightedSum;
                if (bestValues == null)
                    bestValues = new ArrayList<>();
                else
                    bestValues.clear();
                bestValues.add(value);
            } else if(DoubleToolkit.isEqual(bestWeightedSum, weightedSum)) {
                bestValues.add(value);
            }
        } // End of the for cycle over all values

        DefaultISGValue selectedValue = RandomToolkit.random(bestValues);
        if(selectedValue == null) {
            //no value in the bestValues -> select randomly
            selectedValue = values.random();
        }

        // Add the selected value onto the tabu list
        tabuList.put(selectedValue, solution.getIteration() + TABU_DURATION);

        return selectedValue;
    }
}

