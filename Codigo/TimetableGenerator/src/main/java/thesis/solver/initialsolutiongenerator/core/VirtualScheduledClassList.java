package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.*;
import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

import java.util.*;

public class VirtualScheduledClassList implements List<DefaultISGValue> {
    //private List<Time> timeList;
    private final List<Room> roomList;
    private final ClassUnit classUnit;

    //private int index = 0;
    private final List<int[]> parameters = new ArrayList<>();

    public VirtualScheduledClassList(DefaultISGVariable selectedVariable) {
        this.classUnit = selectedVariable.variable();
        DomainModel model = classUnit.getModel();
        this.roomList = model.getRooms();

        TimetableConfiguration timetableConfiguration = model.getTimetableConfiguration();
        //this.timeList = new ArrayList<>();

        for (byte days = 1; days <= timetableConfiguration.getNumDays(); days++) {
            for (short weeks = 1; weeks <= timetableConfiguration.getNumWeeks(); weeks++) {
                for (int length = 1; length < timetableConfiguration.getSlotsPerDay(); length++) {
                    for (int startSlot = 0; startSlot < timetableConfiguration.getSlotsPerDay() - length; startSlot++) {
//                        try {
                            //this.timeList.add(TimeFactory.create(days, weeks, startSlot, length));
                            parameters.add(new int[]{days, weeks, startSlot, length});
//                        } catch (CheckedIllegalArgumentException e) {
//                            // This should never happen
//                            System.out.println(e.getMessage());
//                        }
                    }
                }
            }
        }
    }

    public int size() {
        //return timeList.size() * roomList.size();
        return parameters.size() * roomList.size();
    }

    public DefaultISGValue get(int index) {
        // Gets all the combinations of Rooms and Times.
        // The combinations start by getting all the Rooms for a Timeslot.
        // When there are no rooms the timeslot increments to the next possible one.
        int timeIndex = index / roomList.size();
        int roomIndex = index % roomList.size();

        int[] p = parameters.get(timeIndex);
        Time t = null;
        try {
            t = TimeFactory.create((byte)p[0], (short)p[1], p[2], p[3]);
        } catch (CheckedIllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        Room r = roomList.get(roomIndex);

        return new DefaultISGValue(new ScheduledLesson(classUnit.getModel(), classUnit.getClassId(), r.getRoomId(), t));
    }

    @Override
    public DefaultISGValue set(int index, DefaultISGValue element) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public void add(int index, DefaultISGValue element) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public DefaultISGValue remove(int index) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public int indexOf(Object o) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public ListIterator<DefaultISGValue> listIterator() {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public ListIterator<DefaultISGValue> listIterator(int index) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public List<DefaultISGValue> subList(int fromIndex, int toIndex) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    public Iterator<DefaultISGValue> iterator() {
        return new Iterator<>() {
            int index = 0;
            public boolean hasNext() {
                return index < size();
            }
            public DefaultISGValue next() {
                return get(index++);
            }
        };
    }

    @Override
    public Object[] toArray() {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public boolean add(DefaultISGValue selectedValue) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public boolean addAll(Collection<? extends DefaultISGValue> c) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public boolean addAll(int index, Collection<? extends DefaultISGValue> c) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public void clear() {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public boolean equals(Object o) {
        throw new RuntimeException("Virtual List method not implemented");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("Virtual List method not implemented");
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        throw new RuntimeException("Virtual List method not implemented");
    }
}
