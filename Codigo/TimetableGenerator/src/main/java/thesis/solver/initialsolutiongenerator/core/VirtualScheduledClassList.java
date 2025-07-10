package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.*;
import thesis.utils.RandomToolkit;

import java.util.*;

public class VirtualScheduledClassList implements ISGVirtualValueList<DefaultISGValue> {
    private final List<Room> roomList;
    private final ClassUnit classUnit;
    private final List<Time> possibleTimeBlocks = new ArrayList<>();
    private final DefaultISGVariable variable;

    public VirtualScheduledClassList(DefaultISGVariable selectedVariable) {
        this.classUnit = selectedVariable.variable();
        this.variable = selectedVariable;

        // Get all the rooms defined for this class
        this.roomList = classUnit.getRooms();

        // Get all the time blocks defined for this class
        possibleTimeBlocks.addAll(classUnit.getTimeSet());
    }

    private DefaultISGValue get(int timeNumber, int roomNumber) {
        // Creating values on demand can be taxing on the cpu but it saves memory. The amount
        // of memory saved depends on the number of possible combinations, which in this case
        // are a lot.
        String roomId = !roomList.isEmpty() ? roomList.get(roomNumber).getRoomId() : null;
        Time time = possibleTimeBlocks.get(timeNumber);

        ScheduledLesson scheduledLesson = new ScheduledLesson(classUnit.getModel(), classUnit.getClassId(), roomId, time);
        for(int teacherId : classUnit.getTeacherIdList()) {
            scheduledLesson.addTeacherId(teacherId);
        }

        return new DefaultISGValue(variable, scheduledLesson);
    }

    public DefaultISGValue random() {
        int timeNumber = RandomToolkit.random(possibleTimeBlocks.size());
        int roomNumber = RandomToolkit.random(roomList.size());

        return get(timeNumber, roomNumber);
    }

    @Override
    public Iterator<DefaultISGValue> iterator() {
        return new Iterator<>() {
            private final int maxRoomNumber = roomList.size();
            private final int maxTimeNumber = possibleTimeBlocks.size();
            private int timeNumber = 0;
            private int roomNumber = 0;

            public boolean hasNext() {
                return timeNumber < maxTimeNumber;
            }

            public DefaultISGValue next() {
                DefaultISGValue nextValue = get(timeNumber, roomNumber);

                roomNumber++;

                if (roomNumber >= maxRoomNumber) {
                    roomNumber = 0;
                    timeNumber++;
                }

                return nextValue;
            }
        };
    }
}
