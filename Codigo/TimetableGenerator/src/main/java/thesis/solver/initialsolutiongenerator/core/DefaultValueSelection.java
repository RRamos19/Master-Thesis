package thesis.solver.initialsolutiongenerator.core;

import thesis.utils.RandomToolkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DefaultValueSelection implements ValueSelection<DefaultISGValue, DefaultISGSolution, DefaultISGVariable> {
    private final float iRandomWalkProb = 0.1F;                           // random walk selection
    private final float iWeightCoflicts = 1.0F;                           // weight of a conflict
    private final float iWeightValue = 1.0F;                              // weight of a value (value.toInt())

    private int iTabuSize = 0;                                            // TABU-SEARCH: size of tabu-list
    private final ArrayList<DefaultISGValue> iTabu = null;                // TABU-SEARCH: tabu-list
    private int iTabuPos = 0;                                             // TABU-SEARCH: pointer to the last value in the tabu-list

    private final float iWeightWeightedCoflicts = 1.0F;                   // CBS: CBS weighted conflict weight

    @Override
    public DefaultISGValue selectValue(DefaultISGSolution solution, DefaultISGVariable selectedVariable) {
        ISGVirtualValueList<DefaultISGValue> values = selectedVariable.getValues();
        if(RandomToolkit.random() <= iRandomWalkProb) {
            DefaultISGValue value;
            do {
                //random-walk
                value = values.random();
                // Only return the value if the rooms and teachers are available
            } while(!value.isAvailable());

            return value;
        }

        //values with the lowest weighted sum
        List<DefaultISGValue> bestValues = null;
        double bestWeightedSum = 0;

        //go through all the values
        for(DefaultISGValue value : values) {
            if(!value.isAvailable()) {
                // if the rooms or teachers are not available ignore
                continue;
            }

            if(iTabu != null && iTabu.contains(value)) {
                //value is in the tabu-list
                continue;
            }

            if(Objects.equals(value, selectedVariable.getAssignment())) {
                //do not pick the same value as it is currently assigned
                //if there is a value assigned to the selected variable
                continue;
            }

            // Conflicting values
            Collection<String> conf = solution.getModel().conflictValues(value);

            double weightedConflicts = 0.0; //CBS weighted conflicts
            weightedConflicts = value.getRemovals();

            // Weighted sum of several criteria
            double weightedSum = (iWeightWeightedCoflicts * weightedConflicts) +
                        (iWeightCoflicts * conf.size()) +
                        (iWeightValue * value.toInt());

            //store best values
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
        } //end of the for cycle over all values

        DefaultISGValue selectedValue = RandomToolkit.random(bestValues);
        if(selectedValue == null) {
            do {
                //no value in the bestValues -> select randomly
                selectedValue = values.random();
                // Only return the value if the rooms and teachers are available
            } while(!selectedValue.isAvailable());
        }

        // In case of tabu-search, put into tabu-list
        if(iTabu != null) {
            if (iTabu.size() == iTabuPos)
                iTabu.add(selectedValue);
            else
                iTabu.set(iTabuPos, selectedValue);
            iTabuPos = (iTabuPos + 1) % iTabuSize;
        }

        return selectedValue;
    }
}

