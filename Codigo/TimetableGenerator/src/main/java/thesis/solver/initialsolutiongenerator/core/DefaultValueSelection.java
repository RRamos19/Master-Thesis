package thesis.solver.initialsolutiongenerator.core;

import thesis.utils.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultValueSelection implements ValueSelection<DefaultISGValue, DefaultISGSolution, DefaultISGVariable> {
    private final double iRandomWalkProb = 0.0;                   // random walk selection
    private final double iWeightCoflicts = 1.0;                   // weight of a conflict
    private final double iWeightValue = 0.0;                      // weight of a value (value.toInt())

    private int iTabuSize = 1000;                                  // TABU-SEARCH: size of tabu-list
    private ArrayList<DefaultISGValue> iTabu = new ArrayList<>();  // TABU-SEARCH: tabu-list
    private int iTabuPos = 0;                                      // TABU-SEARCH: pointer to the last value in the tabu-list

//    private final boolean iMPP = false;                           // Minimal perturbations problem
//    private final double iInitialSelectionProb = 0.0;             // MPP: initial selection probability
//    private int iMPPLimit = -1;                                   // MPP: limit on the number of perturbations
    private final double iWeightDeltaInitialAssignment = 0.0;     // MPP: weight of the difference in initial assignments

//    private final ConflictStatistics iStat = null;                // Conflict based statistics (null if not present)
    private final double iWeightWeightedCoflicts = 0.0;           // CBS: CBS weighted conflict weight

//    private final MacPropagation iProp = null;                    // MAC: null if there is no arc-consistency
//    private final boolean iAllowNoGood = false;                   // MAC: allow selection of removed values (MAC+)

    //private final short BEST_K_VALUES = 1000;
    private final short BEST_K_VALUES = 50;

    private final HashMap<DefaultISGVariable, DefaultISGValue> currentSchedules = new HashMap<>();

//    public DefaultISGValue selectValue(DefaultISGSolution solution, DefaultISGVariable selectedVariable) {
////        if (iMPP && selectedVariable.getInitialAssignment() != null) {
////            //Minimal perturbations problem
////            if (solution.unassignedVariables().isEmpty()) {
////                //complete solution – decrease MPP limit if used
////                if (solution.getModel().perturbVariables().size() <= iMPPLimit) {
////                    iMPPLimit = solution.getModel().perturbVariables().size() - 1;
////                }
////            }
////            if (iMPPLimit >= 0 && solution.getModel().perturbVariables().size() > iMPPLimit) {
////                //MPP limit reached – initial value has to be assigned
////                return selectedVariable.getInitialAssignment();
////            }
////            if (RandomUtils.random() <= iInitialSelectionProb) {
////                //with the given probability, initial value is selected
////                return selectedVariable.getInitialAssignment();
////            }
////        } //MPP
//
//        //List<ScheduledLesson> values = selectedVariable.values();
//        List<DefaultISGValue> values = selectedVariable.getValues();
//        if (RandomUtils.random() <= iRandomWalkProb) {
//            //random-walk
//            DefaultISGValue selectedValue = RandomUtils.random(values);
//            currentSchedules.put(selectedVariable, selectedValue);
//            return selectedValue;
//        }
////        if (iProp != null) {
////            //MAC: select one of the not-removed values (usually always)
////            Collection goodValues = iProp.goodValues(selectedVariable);
////            if (!goodValues.isEmpty())
////                values = new Vector(goodValues);
////        } else if (!iAllowNoGood) {
////            //all values are removed and the selection of
////            //not-removed values is prohibited
////            return null;
////        }
//
//        //values with the lowest weighted sum
//        List<DefaultISGValue> bestValues = null;
//        double bestWeightedSum = 0;
//
//        //go through all the values
//        for (DefaultISGValue value : values) {
////            if (iTabu != null && iTabu.contains(value)) {
////                //value is in the tabu-list
////                continue;
////            }
//            if (value.equals(currentSchedules.get(selectedVariable))) {
//                //do not pick the same value as it is currently assigned
//                //if there is a value assigned to the selected variable
//                continue;
//            }
//            //conflicting values
//            Collection<DefaultISGValue> conf = solution.getModel().conflictValues(value);
//            double weightedConflicts = 0.0; //CBS weighted conflicts
////            if (iStat != null) {
////                weightedConflicts = iStat.countRemovals(solution.getIteration(), conf, value));
////            }
//            //MPP: difference in initial assignments
//            long deltaInitialAssignments = 0;
////            if (iMPP) {
////                //go through all conflicts
////                for (Iterator it1 = conf.iterator(); it1.hasNext();) {
////                    Value aValue = (Value)it1.next();
////                    if (aValue.variable().getInitialAssignment() != null) {
////                        //not assigned to an initial value -> good to unassign
////                        deltaInitialAssignments--;
////                    }
////                }
////                if (value.equals(selectedVariable.getInitialAssignment())) {
////                    //value is different from initial value -> bad to assign
////                    deltaInitialAssignments++;
////                }
////                if (iMPPLimit >= 0 && (solution.getModel().perturbVariables().size() + deltaInitialAssignments) > iMPPLimit) {
////                    //assignment exceeds MPP limit
////                    continue;
////                }
////            }
//            //weighted sum of several criteria
//            double weightedSum =
//                    (iWeightDeltaInitialAssignment * deltaInitialAssignments)
//                            + (iWeightWeightedCoflicts * weightedConflicts)
//                            + (iWeightCoflicts * conf.size())
//                            + (iWeightValue * value.toInt());
//            //store best values
//            if (bestValues == null || bestWeightedSum > weightedSum) {
//                bestWeightedSum = weightedSum;
//                if (bestValues == null)
//                    bestValues = new ArrayList<>();
//                else
//                    bestValues.clear();
//                bestValues.add(value);
//            } else if (bestWeightedSum == weightedSum) {
//                bestValues.add(value);
//                if(bestValues.size() > BEST_K_VALUES) {
//                    RandomUtils.removeRandom(bestValues);
//                }
//            }
//        } //end of the for cycle over all values
//
//        DefaultISGValue selectedValue = RandomUtils.random(bestValues);
//        if (selectedValue == null) {
//            //no value in the bestValues -> select randomly
//            selectedValue = RandomUtils.random(values);
//        }
//
//        //In case of tabu-search, put into tabu-list
////        if (iTabu != null) {
////            if (iTabu.size() == iTabuPos)
////                iTabu.add(selectedValue);
////            else
////                iTabu.set(iTabuPos, selectedValue);
////            iTabuPos = (iTabuPos + 1) % iTabuSize;
////        }
//        currentSchedules.put(selectedVariable, selectedValue);
//
//        return selectedValue;
//    }

    class BestValues {
        double bestScore = Double.MAX_VALUE;
        List<DefaultISGValue> bestValues = new ArrayList<>();
    }

    public DefaultISGValue selectValue(DefaultISGSolution solution, DefaultISGVariable selectedVariable) {
        List<DefaultISGValue> values = new VirtualScheduledClassList(selectedVariable);

        int numThreads = 4;

        int chunkSize = (int) Math.ceil(values.size() / (double) numThreads);

        DefaultISGValue currentScheduleValue = currentSchedules.get(selectedVariable);

        List<BestValues> results = IntStream.range(0, numThreads)
        .parallel()
        .mapToObj(i -> {
            final int start = i * chunkSize;
            final int end;
            if(i + 1 < numThreads) {
                end = Math.min(start + chunkSize, values.size());
            } else {
                end = values.size();
            }

            BestValues local = new BestValues();
            for (int j = start; j < end; j++) {
                DefaultISGValue value = values.get(j);

                if(!value.isAvailable()) {
                    // If either the room or the teacher are unavailable, the value is ignored
                    continue;
                }

                if (iTabu != null && iTabu.contains(value)) {
                    //value is in the tabu-list
                    continue;
                }

                if (value.equals(currentScheduleValue)) continue;

                Collection<DefaultISGValue> conf = solution.getModel().conflictValues(value);

                double score =
                        (iWeightDeltaInitialAssignment * 0) +
                                (iWeightWeightedCoflicts * 0.0) +
                                (iWeightCoflicts * conf.size()) +
                                (iWeightValue * value.toInt());


                if (score < local.bestScore) {
                    local.bestValues.clear();
                    local.bestValues.add(value);
                    local.bestScore = score;
                } else if (score == local.bestScore) {
                    local.bestValues.add(value);
                    if (local.bestValues.size() > BEST_K_VALUES)
                        RandomUtils.removeRandom(local.bestValues);
                }
            }

            return local;
        }).collect(Collectors.toList());

        double finalBestScore = Double.MAX_VALUE;
        List<DefaultISGValue> finalBestValues = new ArrayList<>();

        for (BestValues bv : results) {
            if (bv.bestScore < finalBestScore) {
                finalBestScore = bv.bestScore;
                finalBestValues = new ArrayList<>(bv.bestValues);
            } else if (bv.bestScore == finalBestScore) {
                finalBestValues.addAll(bv.bestValues);
            }
        }

        DefaultISGValue selectedValue = RandomUtils.random(finalBestValues);
        if (selectedValue == null) {
            selectedValue = RandomUtils.random(values);
        }

        //In case of tabu-search, put into tabu-list
        if (iTabu != null) {
            if (iTabu.size() == iTabuPos)
                iTabu.add(selectedValue);
            else
                iTabu.set(iTabuPos, selectedValue);
            iTabuPos = (iTabuPos + 1) % iTabuSize;
        }
        currentSchedules.put(selectedVariable, selectedValue);
        return selectedValue;
    }
}

