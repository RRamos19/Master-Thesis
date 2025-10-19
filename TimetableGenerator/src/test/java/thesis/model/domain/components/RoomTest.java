package thesis.model.domain.components;

import org.junit.jupiter.api.Test;
import thesis.model.domain.DataRepository;
import thesis.model.domain.InMemoryRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RoomTest {
    @Test
    public void testRoomDistances() {
        InMemoryRepository repository = new DataRepository();

        Room room1 = RoomFastIdFactory.createRoom("1");
        Room room2 = RoomFastIdFactory.createRoom("2");
        Room room3 = RoomFastIdFactory.createRoom("3");
        Room room4 = RoomFastIdFactory.createRoom("4");

        repository.addRoom(room1);
        repository.addRoom(room2);
        repository.addRoom(room3);
        repository.addRoom(room4);

        room1.addRoomDistance(room2.getRoomId(), 5);
        room1.addRoomDistance(room3.getRoomId(), 10);
        room1.addRoomDistance(room4.getRoomId(), 13);

        room2.addRoomDistance(room3.getRoomId(), 6);
        room2.addRoomDistance(room4.getRoomId(), 4);

        repository.setRoomBidirectionalDistances();

        // Verify distances with room1
        assertEquals(5, room1.getRoomDistance(room2.getIntId()));
        assertEquals(5, room2.getRoomDistance(room1.getIntId()));
        assertEquals(10, room1.getRoomDistance(room3.getIntId()));
        assertEquals(10, room3.getRoomDistance(room1.getIntId()));
        assertEquals(13, room1.getRoomDistance(room4.getIntId()));
        assertEquals(13, room4.getRoomDistance(room1.getIntId()));

        // Verify distances with room2
        assertEquals(6, room2.getRoomDistance(room3.getIntId()));
        assertEquals(6, room3.getRoomDistance(room2.getIntId()));
        assertEquals(4, room2.getRoomDistance(room4.getIntId()));
        assertEquals(4, room4.getRoomDistance(room2.getIntId()));
    }

    @Test
    public void testRoomCleanup() {
        InMemoryRepository repository = new DataRepository();

        Room room1 = RoomFastIdFactory.createRoom("1");
        Room room2 = RoomFastIdFactory.createRoom("2");
        Room room3 = RoomFastIdFactory.createRoom("3");
        Room room4 = RoomFastIdFactory.createRoom("4");

        ClassUnit class1 = new ClassUnit("1");
        ClassUnit class2 = new ClassUnit("2");

        class1.addRoom(room1.getRoomId(), 1);
        class1.addRoom(room2.getRoomId(), 2);

        class2.addRoom(room1.getRoomId(), 1);
        class2.addRoom(room3.getRoomId(), 1);

        repository.addClassUnit(class1);
        repository.addClassUnit(class2);

        repository.addRoom(room1);
        repository.addRoom(room2);
        repository.addRoom(room3);
        repository.addRoom(room4);

        room1.addRoomDistance(room2.getRoomId(), 5);
        room1.addRoomDistance(room3.getRoomId(), 10);
        room1.addRoomDistance(room4.getRoomId(), 13);

        room2.addRoomDistance(room3.getRoomId(), 6);
        room2.addRoomDistance(room4.getRoomId(), 4);

        repository.cleanUnusedData();
        repository.setRoomBidirectionalDistances();

        assertEquals(room1, repository.getRoom(room1.getRoomId()));
        assertEquals(room2, repository.getRoom(room2.getRoomId()));
        assertEquals(room3, repository.getRoom(room3.getRoomId()));
        assertNull(repository.getRoom(room4.getRoomId()));

        // Verify distances with room1
        assertEquals(5, room1.getRoomDistance(room2.getIntId()));
        assertEquals(5, room2.getRoomDistance(room1.getIntId()));
        assertEquals(10, room1.getRoomDistance(room3.getIntId()));
        assertEquals(10, room3.getRoomDistance(room1.getIntId()));
        assertEquals(0, room1.getRoomDistance(room4.getIntId()));
        assertEquals(0, room4.getRoomDistance(room1.getIntId()));

        // Verify distances with room2
        assertEquals(6, room2.getRoomDistance(room3.getIntId()));
        assertEquals(6, room3.getRoomDistance(room2.getIntId()));
        assertEquals(0, room2.getRoomDistance(room4.getIntId()));
        assertEquals(0, room4.getRoomDistance(room2.getIntId()));
    }
}
