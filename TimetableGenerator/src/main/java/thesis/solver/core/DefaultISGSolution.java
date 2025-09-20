package thesis.solver.core;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultISGSolution implements ISGSolution<InMemoryRepository, DefaultISGValue, DefaultISGVariable> {
    private final InMemoryRepository dataModel;
    private final Collection<DefaultISGVariable> variableCollection = new HashSet<>();
    private final Collection<DefaultISGVariable> unassignedVariableCollection = ConcurrentHashMap.newKeySet();
    private List<DefaultISGVariable> bestUnassignedVariableCollection;
    private List<DefaultISGVariable> bestAssignedVariableCollection;
    private Integer bestValue;
    private long iteration = 0;

    // Variables for optimizations
    private final Map<String, Set<ScheduledLesson>> lessonsByRoom;
    private final Map<Integer, Set<ScheduledLesson>> lessonsByTeacher;
    private boolean updateSolution = true;
    private Timetable solution;

    public DefaultISGSolution(InMemoryRepository dataModel) {
        this.dataModel = dataModel;
        this.lessonsByRoom = new HashMap<>();
        this.lessonsByTeacher = new HashMap<>();
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

        // Copy the best cost
        this.bestValue = other.bestValue;

        // Copy the last solution created and the flag to update
        this.updateSolution = other.updateSolution;
        this.solution = other.solution;

        // Deep copy the memory of room and teacher conflicts
        this.lessonsByRoom = new HashMap<>();
        other.lessonsByRoom.forEach((room, set) ->
            this.lessonsByRoom.put(room, new HashSet<>(set))
        );
        this.lessonsByTeacher = new HashMap<>();
        other.lessonsByTeacher.forEach((teacher, set) ->
            this.lessonsByTeacher.put(teacher, new HashSet<>(set))
        );
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
    public void incrementIteration() {
        iteration++;
    }

    @Override
    public long getIteration() {
        return iteration;
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
        ScheduledLesson valueLesson = value.value();
        Set<String> conflicts = new HashSet<>();

        // Add the constraint conflicts
        for (Constraint constraint : value.variable().variable().getConstraintList()) {
            constraint.computeConflicts(valueLesson, timetable, conflicts);
        }

        // Variables to avoid multiple method calls in the for loop
        String valueClassId = valueLesson.getClassId();
        String valueRoomId = valueLesson.getRoomId();
        Time valueTime = valueLesson.getScheduledTime();
        Set<Integer> valueTeachers = valueLesson.getTeacherIds();

        // Add the room conflicts
        if (valueRoomId != null) {
            for (ScheduledLesson scheduledLesson : lessonsByRoom.getOrDefault(valueRoomId, Collections.emptySet())) {
                String classId = scheduledLesson.getClassId();
                if (classId.equals(valueClassId) || conflicts.contains(classId)) continue;

                Room scheduledLessonRoom = scheduledLesson.getRoom();
                int travelTime = scheduledLessonRoom != null ? scheduledLessonRoom.getRoomDistance(RoomFactory.getId(valueRoomId)) : 0;

                // There is only a conflict if the times overlap
                if (scheduledLesson.getScheduledTime().overlaps(valueTime, travelTime)) {
                    conflicts.add(classId);
                }
            }
        }

        // Add the teacher conflicts
        for (int teacherId : valueTeachers) {
            for (ScheduledLesson scheduledLesson : lessonsByTeacher.getOrDefault(teacherId, Collections.emptySet())) {
                String classId = scheduledLesson.getClassId();
                if (classId.equals(valueClassId) || conflicts.contains(classId)) continue;

                // There is only a conflict if the times overlap
                if (scheduledLesson.getScheduledTime().overlaps(valueTime)) {
                    conflicts.add(classId);
                }
            }
        }

        return conflicts;
    }

    @Override
    public void convertToAssigned(DefaultISGVariable var) {
        if(!unassignedVariableCollection.contains(var)) {
            // Should be impossible, unless there is a bug
            throw new RuntimeException("Unassigned variable was not found!");
        }

        unassignedVariableCollection.remove(var);
        variableCollection.add(var);

        addToMemory(var.getAssignment().value());

        updateSolution = true;
    }

    private void addToMemory(ScheduledLesson scheduledLesson) {
        String roomId = scheduledLesson.getRoomId();
        if(roomId != null) {
            lessonsByRoom.computeIfAbsent(roomId, k -> new HashSet<>()).add(scheduledLesson);
        }

        for(Integer teacherId : scheduledLesson.getTeacherIds()) {
            lessonsByTeacher.computeIfAbsent(teacherId, k -> new HashSet<>()).add(scheduledLesson);
        }
    }

    @Override
    public void convertToUnassigned(DefaultISGVariable var) {
        if(!variableCollection.contains(var)) {
            // Should be impossible, unless there is a bug
            System.out.println("Help!!");
            System.out.println(var);
            System.out.println(variableCollection);
            System.out.println();
            throw new RuntimeException("Assigned variable was not found!");
        }

        variableCollection.remove(var);
        unassignedVariableCollection.add(var);

        removeFromMemory(var.getAssignment().value());

        updateSolution = true;
    }

    private void removeFromMemory(ScheduledLesson scheduledLesson) {
        String roomId = scheduledLesson.getRoomId();
        if(roomId != null) {
            Set<ScheduledLesson> lessonSet = lessonsByRoom.get(roomId);
            if(lessonSet != null) lessonSet.remove(scheduledLesson);
        }

        for(Integer teacherId : scheduledLesson.getTeacherIds()) {
            Set<ScheduledLesson> lessonSet = lessonsByTeacher.get(teacherId);
            if(lessonSet != null) lessonSet.remove(scheduledLesson);
        }
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

        lessonsByRoom.clear();
        lessonsByTeacher.clear();
        for(DefaultISGVariable variable : variableCollection) {
            addToMemory(variable.getAssignment().value());
        }

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
