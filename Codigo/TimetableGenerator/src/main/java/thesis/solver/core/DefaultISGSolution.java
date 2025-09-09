package thesis.solver.core;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;

import java.util.*;

public class DefaultISGSolution implements ISGSolution<InMemoryRepository, DefaultISGValue, DefaultISGVariable> {
    private final InMemoryRepository dataModel;
    private final Collection<DefaultISGVariable> variableCollection = new ArrayList<>();
    private final Collection<DefaultISGVariable> unassignedVariableCollection = new Vector<>();
    private List<DefaultISGVariable> bestUnassignedVariableCollection;
    private List<DefaultISGVariable> bestAssignedVariableCollection;
    private Integer bestValue;

    private boolean updateSolution = true;
    private Timetable solution;

    public DefaultISGSolution(InMemoryRepository dataModel) {
        this.dataModel = dataModel;
    }

    // Deep copy of another solution
    public DefaultISGSolution(DefaultISGSolution other) {
        this.dataModel = other.dataModel;

        // Copy the collections of assigned and unassigned variables
        other.variableCollection.forEach((var) -> {
            this.variableCollection.add(new DefaultISGVariable(this, var));
        });
        other.unassignedVariableCollection.forEach((var) -> {
            this.unassignedVariableCollection.add(new DefaultISGVariable(this, var));
        });

        // If there was a saveBest copy the best assigned and unassigned variables
        if(other.bestUnassignedVariableCollection != null) {
            this.bestUnassignedVariableCollection = new ArrayList<>();

            other.bestUnassignedVariableCollection.forEach((var) -> {
                this.bestUnassignedVariableCollection.add(new DefaultISGVariable(this, var));
            });
        }

        if(other.bestAssignedVariableCollection != null) {
            this.bestAssignedVariableCollection = new ArrayList<>();

            other.bestAssignedVariableCollection.forEach((var) -> {
                this.bestAssignedVariableCollection.add(new DefaultISGVariable(this, var));
            });
        }

        // Copy the cost
        this.bestValue = other.bestValue;

        this.updateSolution = other.updateSolution;
        this.solution = other.solution;

        // Update the solution
//        this.solution();
    }

    @Override
    public Timetable solution() {
        if(updateSolution) {
            solution = new Timetable(dataModel.getProgramName());
            solution.bindDataModel(dataModel);

            for (DefaultISGVariable variable : variableCollection) {
                solution.addScheduledLesson(variable.getAssignment().value());
            }

            updateSolution = false;
        }

        return solution;
    }

    @Override
    public Collection<DefaultISGVariable> getUnassignedVariables() {
        return unassignedVariableCollection;
    }

    @Override
    public Collection<DefaultISGVariable> getAssignedVariables() {
        return variableCollection;
    }

    @Override
    public Collection<DefaultISGVariable> getBestUnassignedVariables() {
        return bestUnassignedVariableCollection;
    }

    @Override
    public Boolean wasBestSaved() {
        return bestValue != null;
    }

    @Override
    public int getBestValue() {
        return bestValue;
    }

    @Override
    public int getTotalValue() {
        return solution().cost();
    }

    public InMemoryRepository getDataModel() {
        return dataModel;
    }

    @Override
    public void addUnassignedVariable(DefaultISGVariable var) {
        unassignedVariableCollection.add(var);
    }

    @Override
    public boolean isSolutionValid() {
        return unassignedVariableCollection.isEmpty() && solution().isValid();
    }

    @Override
    public Set<String> conflictIds(DefaultISGValue value) {
        Timetable timetable = solution();
        Set<String> conflictIds = new HashSet<>();

        // Add the constraint conflicts
        for (Constraint constraint : value.variable().variable().getConstraintList()) {
            constraint.computeConflicts(value.value(), timetable, conflictIds);
        }

        // Variables to avoid multiple method calls in the for loop
        ScheduledLesson valueLesson = value.value();
        String valueClassId = valueLesson.getClassId();
        String valueRoomId = valueLesson.getRoomId();
        Time valueTime = valueLesson.getScheduledTime();
        List<Integer> valueTeachers = valueLesson.getTeacherIds();

        // Add the room and teacher conflicts
        for (DefaultISGVariable variable : variableCollection) {
            ScheduledLesson scheduledLesson = variable.getAssignment().value();

            if (scheduledLesson != null) {
                String lessonRoomId = scheduledLesson.getRoomId();
                String lessonClassId = scheduledLesson.getClassId();
                List<Integer> lessonTeachers = scheduledLesson.getTeacherIds();

                // If the class ids are the same skip
                if (Objects.equals(lessonClassId, valueClassId)) {
                    continue;
                }

                if(scheduledLesson.getScheduledTime().overlaps(valueTime)) {
                    // Verify if the rooms are the same
                    if (lessonRoomId != null && Objects.equals(lessonRoomId, valueRoomId)) {
                        conflictIds.add(lessonClassId);
                        continue;
                    }

                    // Verify if any of the teachers involved in both lessons overlap
                    if (!valueTeachers.isEmpty() && !lessonTeachers.isEmpty()) {
                        for (int teacherId : valueTeachers) {
                            if (lessonTeachers.contains(teacherId)) {
                                conflictIds.add(lessonClassId);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return conflictIds;
    }

    @Override
    public void convertToAssigned(DefaultISGVariable var) {
        if(!unassignedVariableCollection.contains(var)) {
            // Should be impossible, unless there is a bug
            throw new RuntimeException("Unassigned variable was not found!");
        }

        unassignedVariableCollection.remove(var);
        variableCollection.add(var);

        updateSolution = true;
    }

    @Override
    public void convertToUnassigned(DefaultISGVariable var) {
        if(!variableCollection.contains(var)) {
            // Should be impossible, unless there is a bug
            throw new RuntimeException("Assigned variable was not found!");
        }

        variableCollection.remove(var);
        unassignedVariableCollection.add(var);

        updateSolution = true;
    }

    @Override
    public void saveBest() {
        bestUnassignedVariableCollection = new ArrayList<>(unassignedVariableCollection);
        bestUnassignedVariableCollection.forEach(DefaultISGVariable::saveBest);

        bestAssignedVariableCollection = new ArrayList<>(variableCollection);
        bestAssignedVariableCollection.forEach(DefaultISGVariable::saveBest);

        bestValue = getTotalValue();
    }

    @Override
    public void restoreBest() {
        unassignedVariableCollection.clear();
        unassignedVariableCollection.addAll(bestUnassignedVariableCollection);
        unassignedVariableCollection.forEach(DefaultISGVariable::restoreBest);

        variableCollection.clear();
        variableCollection.addAll(bestAssignedVariableCollection);
        variableCollection.forEach(DefaultISGVariable::restoreBest);

        updateSolution = true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGSolution)) return false;
        DefaultISGSolution that = (DefaultISGSolution) o;
        return Objects.equals(variableCollection, that.variableCollection) &&
                Objects.equals(unassignedVariableCollection, that.unassignedVariableCollection) &&
                Objects.equals(bestValue, that.bestValue) &&
                Objects.equals(bestUnassignedVariableCollection, that.bestUnassignedVariableCollection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableCollection, unassignedVariableCollection, bestAssignedVariableCollection, bestUnassignedVariableCollection, bestValue);
    }
}
