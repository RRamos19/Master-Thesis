package thesis.service.initialsolutiongenerator;

import thesis.model.domain.*;
import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

import java.util.*;

public class VirtualScheduleClassValueList implements List<ScheduledLesson> {
    List<Time> timeList;
    List<Room> roomList;
    ClassUnit classUnit;

    public VirtualScheduleClassValueList(ClassUnit classUnit) {
        this.classUnit = classUnit;
        DomainModel model = classUnit.getModel();
        this.roomList = model.getRooms();

        TimetableConfiguration timetableConfiguration = model.getTimetableConfiguration();
        this.timeList = new ArrayList<>();

        for (byte days = 1; days <= timetableConfiguration.getNumDays(); days++) {
            for (short weeks = 1; weeks <= timetableConfiguration.getNumWeeks(); weeks++) {
                for (int length = 1; length < timetableConfiguration.getSlotsPerDay(); length++) {
                    for (int startSlot = 0; startSlot < timetableConfiguration.getSlotsPerDay() - length; startSlot++) {
                        try {
                            this.timeList.add(TimeFactory.create(days, weeks, startSlot, length));
                        } catch (CheckedIllegalArgumentException e) {
                            // This should never happen
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public int size() {
        return timeList.size() * roomList.size();
    }

    public ScheduledLesson get(int index) {
        int timeIndex = index / roomList.size();
        int roomIndex = index % roomList.size();
        Time t = timeList.get(timeIndex);
        Room r = roomList.get(roomIndex);
        return new ScheduledLesson(classUnit.getModel(), classUnit.getClassId(), r.getRoomId(), t);
    }

    @Override
    public ScheduledLesson set(int index, ScheduledLesson element) {
        return null;
    }

    @Override
    public void add(int index, ScheduledLesson element) {

    }

    @Override
    public ScheduledLesson remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<ScheduledLesson> listIterator() {
        return null;
    }

    @Override
    public ListIterator<ScheduledLesson> listIterator(int index) {
        return null;
    }

    @Override
    public List<ScheduledLesson> subList(int fromIndex, int toIndex) {
        return List.of();
    }

    public Iterator<ScheduledLesson> iterator() {
        return new Iterator<>() {
            int index = 0;
            public boolean hasNext() {
                return index < size();
            }
            public ScheduledLesson next() {
                return get(index++);
            }
        };
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(ScheduledLesson scheduledLesson) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends ScheduledLesson> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends ScheduledLesson> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }
}
