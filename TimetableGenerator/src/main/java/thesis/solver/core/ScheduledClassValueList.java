package thesis.solver.core;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.ClassUnit;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Time;
import thesis.utils.RandomToolkit;

import java.util.*;

public class ScheduledClassValueList implements ISGValueList<DefaultISGValue> {

    private final InMemoryRepository dataModel;
    private final DefaultISGVariable selectedVariable;

    public ScheduledClassValueList(InMemoryRepository dataModel, DefaultISGVariable selectedVariable) {
        if (selectedVariable == null) {
            throw new IllegalStateException("ScheduledClassValueList: The selected variable shouldn't be null");
        }
        this.dataModel = dataModel;
        this.selectedVariable = selectedVariable;
    }

    @Override
    public DefaultISGValue random() {
        // Not efficient, creates every value to choose a random one
        List<DefaultISGValue> all = new ArrayList<>();
        for (DefaultISGValue v : this) {
            all.add(v);
        }
        return RandomToolkit.random(all);
    }

    @Override
    public List<DefaultISGValue> values() {
        // Not efficient, creates every value
        List<DefaultISGValue> all = new ArrayList<>();
        for (DefaultISGValue v : this) {
            all.add(v);
        }
        return all;
    }

    @Override
    public Iterator<DefaultISGValue> iterator() {
        ClassUnit classUnit = selectedVariable.variable();
        String classId = classUnit.getClassId();
        List<String> roomList = new ArrayList<>(classUnit.getRoomIds());
        if (roomList.isEmpty()) {
            roomList.add(null);
        }
        List<Time> times = new ArrayList<>(classUnit.getTimeSet());
        List<Integer> teachers = new ArrayList<>(classUnit.getTeacherIdList());

        return new Iterator<>() {
            private int roomIndex = 0;
            private int timeIndex = 0;
            private DefaultISGValue nextValue = null;

            private void computeNext() {
                nextValue = null;
                while (roomIndex < roomList.size()) {
                    String roomId = roomList.get(roomIndex);
                    Time time = times.get(timeIndex);

                    ScheduledLesson scheduledLesson = new ScheduledLesson(classId, roomId, time);
                    scheduledLesson.bindModel(dataModel);

                    for(int teacherId : teachers) {
                        scheduledLesson.addTeacherId(teacherId);
                    }

                    // Move the indexes
                    timeIndex++;
                    if (timeIndex >= times.size()) {
                        timeIndex = 0;
                        roomIndex++;
                    }

                    if (scheduledLesson.isAvailable()) {
                        nextValue = new DefaultISGValue(selectedVariable, scheduledLesson);
                        return;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                if (nextValue == null) {
                    computeNext();
                }
                return nextValue != null;
            }

            @Override
            public DefaultISGValue next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                DefaultISGValue result = nextValue;
                nextValue = null;
                return result;
            }
        };
    }
}
