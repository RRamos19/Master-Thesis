package thesis.solver.core;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.*;

import java.util.*;

public class DefaultISGSolution implements ISGSolution<InMemoryRepository, DefaultISGValue, DefaultISGVariable> {
    private final InMemoryRepository dataModel;
    private final List<DefaultISGVariable> variableList = Collections.synchronizedList(new ArrayList<>());
    private final List<DefaultISGVariable> unassignedVariableList = Collections.synchronizedList(new ArrayList<>());
    private List<DefaultISGVariable> bestUnassignedVariableList;
    private List<DefaultISGVariable> bestAssignedVariableList;
    private Integer bestValue;

    public DefaultISGSolution(InMemoryRepository dataModel) {
        this.dataModel = dataModel;
    }

    // Deep copy of another solution
    public DefaultISGSolution(DefaultISGSolution other) {
        this.dataModel = other.dataModel;

        other.variableList.forEach((var) -> {
            this.variableList.add(new DefaultISGVariable(this, var));
        });
        other.unassignedVariableList.forEach((var) -> {
            this.unassignedVariableList.add(new DefaultISGVariable(this, var));
        });

        if(other.bestUnassignedVariableList != null) {
            this.bestUnassignedVariableList = new ArrayList<>();

            other.bestUnassignedVariableList.forEach((var) -> {
                this.bestUnassignedVariableList.add(new DefaultISGVariable(this, var));
            });
        }

        if(other.bestAssignedVariableList != null) {
            this.bestAssignedVariableList = new ArrayList<>();

            other.bestAssignedVariableList.forEach((var) -> {
                this.bestAssignedVariableList.add(new DefaultISGVariable(this, var));
            });
        }

        this.bestValue = other.bestValue;
    }

    @Override
    public Timetable solution() {
        Timetable timetable = new Timetable(dataModel.getProgramName());
        timetable.bindDataModel(dataModel);

        for(DefaultISGVariable var : variableList) {
            timetable.addScheduledLesson(var.getAssignment().value());
        }

        return timetable;
    }

    @Override
    public List<DefaultISGVariable> getUnassignedVariables() {
        return unassignedVariableList;
    }

    @Override
    public List<DefaultISGVariable> getAssignedVariables() {
        return variableList;
    }

    @Override
    public List<DefaultISGVariable> getBestUnassignedVariables() {
        return bestUnassignedVariableList;
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
        unassignedVariableList.add(var);
    }

    @Override
    public boolean isSolutionValid() {
        if(!unassignedVariableList.isEmpty())
            return false;

        // Should not be needed but just for double checking
        for(DefaultISGVariable var : variableList) {
            if(var.getAssignment() == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<Integer> conflictValues(DefaultISGValue value) {
        List<Integer> conflictValues = new ArrayList<>();

        Timetable timetable = solution();

        // Add the constraint penalties
        for (Constraint constraint : value.variable().variable().getConstraintList()) {
            conflictValues.addAll(constraint.computePenaltiesIfScheduled(value.value(), timetable));
        }

        return conflictValues;
    }

    @Override
    public Set<String> conflictIds(DefaultISGValue value) {
        HashSet<String> conflictIds = new HashSet<>();

        Timetable timetable = solution();

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
        for (DefaultISGVariable variable : variableList) {
            ScheduledLesson scheduledLesson = variable.getAssignment().value();

            if (scheduledLesson != null) {
                String lessonRoomId = scheduledLesson.getRoomId();
                String lessonClassId = scheduledLesson.getClassId();
                List<Integer> lessonTeachers = scheduledLesson.getTeacherIds();

                if (Objects.equals(lessonClassId, valueClassId)) continue;

                if (lessonRoomId != null && Objects.equals(lessonRoomId, valueRoomId)) {
                    if (scheduledLesson.getScheduledTime().overlaps(valueTime)) {
                        conflictIds.add(lessonClassId);
                        continue;
                    }
                }

                if(!valueTeachers.isEmpty() && !lessonTeachers.isEmpty()) {
                    for(int teacherId : valueTeachers) {
                        if(lessonTeachers.contains(teacherId)) {
                            conflictIds.add(lessonClassId);
                            break;
                        }
                    }
                }
            }
        }

        return conflictIds;
    }

    @Override
    public void convertToAssigned(DefaultISGVariable var) {
        if(!unassignedVariableList.contains(var)) {
            // Should be impossible, unless there is a bug
            throw new RuntimeException("Unassigned variable was not found!");
        }

        unassignedVariableList.remove(var);
        variableList.add(var);
    }

    @Override
    public void convertToUnassigned(DefaultISGVariable var) {
        if(!variableList.contains(var)) {
            // Should be impossible, unless there is a bug
            throw new RuntimeException("Assigned variable was not found!");
        }

        variableList.remove(var);
        unassignedVariableList.add(var);
    }

    @Override
    public void saveBest() {
        bestUnassignedVariableList = new ArrayList<>(unassignedVariableList);
        bestUnassignedVariableList.forEach(DefaultISGVariable::saveBest);

        bestAssignedVariableList = new ArrayList<>(variableList);
        bestAssignedVariableList.forEach(DefaultISGVariable::saveBest);

        bestValue = getTotalValue();
    }

    @Override
    public void restoreBest() {
        unassignedVariableList.clear();
        unassignedVariableList.addAll(bestUnassignedVariableList);
        unassignedVariableList.forEach(DefaultISGVariable::restoreBest);

        variableList.clear();
        variableList.addAll(bestAssignedVariableList);
        variableList.forEach(DefaultISGVariable::restoreBest);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGSolution)) return false;
        DefaultISGSolution that = (DefaultISGSolution) o;
        return Objects.equals(variableList, that.variableList) &&
                Objects.equals(unassignedVariableList, that.unassignedVariableList) &&
                Objects.equals(bestValue, that.bestValue) &&
                Objects.equals(bestUnassignedVariableList, that.bestUnassignedVariableList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableList, unassignedVariableList, dataModel, bestValue);
    }
}
