package thesis.solver.core;

import thesis.model.domain.*;
import thesis.utils.RandomToolkit;

import java.util.*;

public class ScheduledClassValueList implements ISGValueList<DefaultISGValue> {
    private final List<DefaultISGValue> defaultISGValueList = new ArrayList<>();

    public ScheduledClassValueList(DefaultISGVariable selectedVariable) {
        ClassUnit classUnit = selectedVariable.variable();
        DataRepository dataRepository = classUnit.getModel();
        String classId = classUnit.getClassId();

        // Copy of the original set to allow modifications
        Set<String> roomList = new HashSet<>(classUnit.getRoomIds());

        // If there are no rooms for a given class, a null value is used
        if(roomList.isEmpty()) {

            roomList.add(null);
        }

        // Create all the possible values and only store the ones that are available
        // (The lesson is available only if the Room and Teachers are available at the scheduled time)
        for(String roomId : roomList) {
            for (Time time : classUnit.getTimeSet()) {
                ScheduledLesson scheduledLesson = new ScheduledLesson(dataRepository, classId, roomId, time);

                if (scheduledLesson.isAvailable()) {
                    defaultISGValueList.add(new DefaultISGValue(selectedVariable, scheduledLesson));
                }
            }
        }
    }

    @Override
    public DefaultISGValue random() {
        return RandomToolkit.random(defaultISGValueList);
    }

    @Override
    public List<DefaultISGValue> values() {
        return defaultISGValueList;
    }
}
