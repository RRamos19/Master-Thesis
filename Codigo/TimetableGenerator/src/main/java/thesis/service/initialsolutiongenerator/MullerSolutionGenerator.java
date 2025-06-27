package thesis.service.initialsolutiongenerator;

import thesis.model.domain.*;
import thesis.utils.RandomUtils;

import java.util.List;

/**
 * The solution generator presented is based on Tomáš Müller's phd thesis.
 * Source - Constraint Based Timetabling https://muller.unitime.org/phd-thesis.pdf
 */
public class MullerSolutionGenerator implements InitialSolutionGenerator<Timetable, ClassUnit> {
    private boolean interruptAlgorithm = false;
    private final DomainModel data;
    private Timetable bestSolution = null;
    private List<ClassUnit> unassignedVariables = null;


    public MullerSolutionGenerator(DomainModel data) {
        this.data = data;
    }


    public Timetable generate(List<ClassUnit> unscheduled, int maxIterations) {
        Timetable schedule = new Timetable(data);
        schedule.setUnscheduledLessons(unscheduled);
        ValueSelection<ScheduledLesson, Timetable, ClassUnit> valueSelection = new ClassValueSelection();
        unassignedVariables = unscheduled;

        int iter = 0;
        while(!unscheduled.isEmpty() && iter < maxIterations && !interruptAlgorithm) {
            iter++;
            ClassUnit variable = selectVariable(schedule, unscheduled);

            assert(variable != null); // Should be impossible because the list of unscheduled is based on the data in DomainModel

            ScheduledLesson value = valueSelection.selectValue(schedule, variable);

            if(value != null) {
                unscheduled.remove(variable);
            }

            if(isBetterThanBestSolution(schedule)) {
                bestSolution = schedule.clone();
            }
        }

        return bestSolution;
    }


    private boolean isBetterThanBestSolution(Timetable solution) {
        if(bestSolution == null) {
            return true;
        }

        // Should be impossible
        assert(unassignedVariables != null);

        int currentUnassigned = solution.getUnscheduledLessons().size();

        int bestUnassigned = bestSolution.getUnscheduledLessons().size();

        if (bestUnassigned != currentUnassigned) {
            return bestUnassigned > currentUnassigned;
        }

        int currentValue = solution.getTotalValue();
        int bestValue = bestSolution.getBestValue();

        return currentValue < bestValue;
    }


    /**
     * Choose a class at random. The list of unscheduled classes is prioritized if there are classes that still need to be scheduled. Otherwise, a random already scheduled class is chosen to be rescheduled.
     * @param schedule Current schedule which basically is a list of classes with assigned time blocks
     * @param unscheduled List of classes to be scheduled. If the list is empty that schedule is considered complete, although is can still be optimized.
     * @return Class id of the selected class
     */
    private ClassUnit selectVariable(Timetable schedule, List<ClassUnit> unscheduled) {
        if(!unscheduled.isEmpty()) {
            return RandomUtils.random(unscheduled);
        } else {
            return data.getClassUnit(RandomUtils.random(schedule.getScheduledLessonList()).getClassId());
        }
    }


    private void stopAlgorithm() {
        interruptAlgorithm = true;
    }
}
