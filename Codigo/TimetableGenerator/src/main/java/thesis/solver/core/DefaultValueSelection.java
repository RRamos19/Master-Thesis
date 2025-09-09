package thesis.solver.core;

import thesis.utils.RandomToolkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DefaultValueSelection implements ValueSelection<DefaultISGValue, DefaultISGSolution, DefaultISGVariable> {
    private final float iRandomWalkProb = 0.05F;                          // random walk selection
    private final float iWeightCoflicts = 1.0F;                           // weight of a conflict
    private final float iWeightValue = 0.0F;                              // weight of a value (value.toInt())

    private final float iWeightWeightedCoflicts = 1.0F;                   // CBS: CBS weighted conflict weight

    @Override
    public DefaultISGValue selectValue(DefaultISGSolution solution, DefaultISGVariable selectedVariable) {
        ISGValueList<DefaultISGValue> values = selectedVariable.getValues();

        if(RandomToolkit.random() <= iRandomWalkProb) {
            //random-walk
            return values.random();
        }

        // values with the lowest weighted sum
        List<DefaultISGValue> bestValues = null;
        double bestWeightedSum = 0;

        // go through all the values
        for(DefaultISGValue value : values.values()) {
            if(Objects.equals(value, selectedVariable.getAssignment())) {
                // do not pick the same value as it is currently assigned
                // if there is a value assigned to the selected variable
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
            } else if(bestWeightedSum == weightedSum) {
                bestValues.add(value);
            }
        } // End of the for cycle over all values

        DefaultISGValue selectedValue = RandomToolkit.random(bestValues);
        if(selectedValue == null) {
            //no value in the bestValues -> select randomly
            selectedValue = values.random();
        }

        return selectedValue;
    }
}

