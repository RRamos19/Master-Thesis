package thesis.model.domain.components;

import org.junit.jupiter.api.Test;
import thesis.model.domain.DataRepository;
import thesis.model.exceptions.CheckedIllegalArgumentException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduledLessonTest {
    @Test
    public void testEquals() throws CheckedIllegalArgumentException {
        String classId1 = "1";
        String roomId1 = "1";
        String classId2 = "2";
        String roomId2 = "2";
        Time time1 = TimeFactory.create("1111111", "111111111111", (short) 20, (short) 10);
        Time time2 = TimeFactory.create("1111111", "111111111111", (short) 10, (short) 12);
        ScheduledLesson scheduledLesson1 = new ScheduledLesson(classId1, roomId1, time1);
        ScheduledLesson scheduledLesson2 = new ScheduledLesson(classId1, roomId1, time1);
        ScheduledLesson scheduledLesson3 = new ScheduledLesson(classId2, roomId1, time1);
        ScheduledLesson scheduledLesson4 = new ScheduledLesson(classId1, roomId2, time1);
        ScheduledLesson scheduledLesson5 = new ScheduledLesson(classId1, roomId1, time2);
        ScheduledLesson scheduledLesson6 = new ScheduledLesson(classId2, roomId2, time1);
        ScheduledLesson scheduledLesson7 = new ScheduledLesson(classId2, roomId1, time2);
        ScheduledLesson scheduledLesson8 = new ScheduledLesson(classId2, roomId2, time2);

        assertEquals(scheduledLesson1, scheduledLesson2);

        List<ScheduledLesson> lessonList = List.of(scheduledLesson1, scheduledLesson3, scheduledLesson4, scheduledLesson5, scheduledLesson6, scheduledLesson7, scheduledLesson8);
        for(int i=0; i<lessonList.size(); i++) {
            for(int j=i+1; j < lessonList.size(); j++) {
                assertNotEquals(lessonList.get(i), lessonList.get(j));
            }
        }
    }

    @Test
    public void testCollectionContains() throws CheckedIllegalArgumentException {
        Collection<ScheduledLesson> list = new ArrayList<>();
        Collection<ScheduledLesson> set = new HashSet<>();

        String classId1 = "1";
        String roomId1 = "1";
        String roomId2 = "2";
        String classId2 = "2";
        Time time1 = TimeFactory.create("1111111", "111111111111", (short) 20, (short) 10);
        ScheduledLesson scheduledLesson1 = new ScheduledLesson(classId1, roomId1, time1);
        ScheduledLesson scheduledLesson2 = new ScheduledLesson(classId2, roomId1, time1);
        ScheduledLesson scheduledLesson3 = new ScheduledLesson(classId2, roomId2, time1);

        list.add(scheduledLesson1);
        list.add(scheduledLesson2);
        set.add(scheduledLesson2);

        assertTrue(list.contains(scheduledLesson1));
        assertTrue(list.contains(scheduledLesson2));
        assertFalse(list.contains(scheduledLesson3));

        assertFalse(set.contains(scheduledLesson1));
        assertTrue(set.contains(scheduledLesson2));
        assertFalse(set.contains(scheduledLesson3));
    }

    @Test
    public void testUnavailabilities() throws CheckedIllegalArgumentException {
        DataRepository data = new DataRepository("test");
        String classId1 = "1";
        String roomId1 = "1";
        String roomId2 = "2";
        String classId2 = "2";
        Time time1 = TimeFactory.create("1111111", "111111111111", (short) 20, (short) 10);
        ScheduledLesson scheduledLesson1 = new ScheduledLesson(classId1, roomId1, time1);
        ScheduledLesson scheduledLesson2 = new ScheduledLesson(classId2, roomId2, time1);

        scheduledLesson1.bindModel(data);
        scheduledLesson2.bindModel(data);

        Room room1 = RoomFactory.createRoom(roomId1);
        room1.addUnavailability(time1);

        Room room2 = RoomFactory.createRoom(roomId2);

        data.addRoom(room1);
        data.addRoom(room2);

        assertFalse(scheduledLesson1.isAvailable());
        assertTrue(scheduledLesson2.isAvailable());
    }
}
